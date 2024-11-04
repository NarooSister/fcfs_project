package com.sparta.orderservice.service;


import com.sparta.orderservice.client.TicketClient;
import com.sparta.orderservice.dto.OrderDto;
import com.sparta.orderservice.dto.OrderRequestDto;
import com.sparta.orderservice.dto.OrderedTicketDto;
import com.sparta.orderservice.dto.TicketDto;
import com.sparta.orderservice.entity.OrderedTicket;
import com.sparta.orderservice.entity.Orders;
import com.sparta.orderservice.event.StockDecrEvent;
import com.sparta.orderservice.event.StockIncrEvent;
import com.sparta.orderservice.exception.OrderBusinessException;
import com.sparta.orderservice.exception.OrderServiceErrorCode;
import com.sparta.orderservice.repository.OrderRepository;
import com.sparta.orderservice.repository.OrderedTicketRepository;

import com.sparta.orderservice.toss.PaymentResponse;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class OrderService {
    private final OrderRepository orderRepository;
    private final OrderConfirmationService orderConfirmationService;
    private final OrderedTicketRepository orderedTicketRepository;
    private final TicketClient ticketClient;
    private final RefundService refundService;
    private final KafkaTemplate<String, StockDecrEvent> kafkaDecrTemplate;
    private final KafkaTemplate<String, StockIncrEvent> kafkaIncrTemplate;
    private final RedisTemplate<String, Integer> redisTemplate;
    private static final String STOCK_DECREMENT_TOPIC = "stock-decrement-topic";
    private static final String STOCK_RESTORE_TOPIC = "stock-restore-topic";

    public OrderService(OrderRepository orderRepository, OrderConfirmationService orderConfirmationService, OrderedTicketRepository orderedTicketRepository, RefundService refundService, TicketClient ticketClient, KafkaTemplate<String, StockDecrEvent> kafkaDecrTemplate, KafkaTemplate<String, StockIncrEvent> kafkaIncrTemplate, RedisTemplate<String, Integer> redisTemplate) {
        this.orderRepository = orderRepository;
        this.orderConfirmationService = orderConfirmationService;
        this.orderedTicketRepository = orderedTicketRepository;
        this.refundService = refundService;
        this.ticketClient = ticketClient;
        this.kafkaDecrTemplate = kafkaDecrTemplate;
        this.kafkaIncrTemplate = kafkaIncrTemplate;
        this.redisTemplate = redisTemplate;
    }

    // 사용자의 모든 주문 가져오기
    public List<OrderDto> readAllOrdersByUser(String username) {
        return Optional.ofNullable(orderRepository.findAllByUsername(username))
                .filter(orderList -> !orderList.isEmpty())
                .orElseThrow(() -> new OrderBusinessException(OrderServiceErrorCode.ALL_ORDER_NOT_FOUND))
                .stream()
                .map(OrderDto::new)
                .toList();
    }

    // 사용자의 주문 상세 내역 가져오기
    public OrderDto readOrderByUser(String username, Long orderId) {
        return orderRepository.findByIdAndUsername(orderId, username)
                .map(OrderDto::new)
                .orElseThrow(() -> new OrderBusinessException(OrderServiceErrorCode.ORDER_NOT_FOUND));
    }

    public void createOrder(String username, OrderRequestDto orderRequestDto) {
        // 주문 생성
        Orders order = new Orders(username);
        orderRepository.save(order);

        // 주문 항목 및 총 금액 계산
        int totalPrice = processOrderItems(order, orderRequestDto.getOrderedTickets());

        // 재고 확인 및 차감
        validateAndReduceStock(orderRequestDto);

        // 주문 최종 금액 업데이트
        order.updateTotalPrice(totalPrice);
        orderRepository.save(order);

        // 재고 차감 이벤트 전송
        sendStockDecrementEvent(order);

        try {
            // 임의의 paymentKey 생성
            String paymentKey = UUID.randomUUID().toString();
            PaymentResponse paymentResponse = simulatePaymentVerification(paymentKey);
            // 결제와 주문 확정 로직(트랜잭션 설정)
            orderConfirmationService.confirmOrderAndPayment(order, paymentResponse);
        } catch (Exception e) {
            // 결제 실패 시 주문 취소 및 재고 복구 처리
            processOrderCancellation(order.getId());
            throw new OrderBusinessException(OrderServiceErrorCode.PAYMENT_FAILED);
        }
    }

    private void processOrderCancellation(Long orderId) {
        Orders order = orderRepository.findById(orderId)
                .orElseThrow(() -> new OrderBusinessException(OrderServiceErrorCode.ORDER_NOT_FOUND));

        order.cancelOrder();
        orderRepository.save(order);

        List<OrderedTicket> orderedTickets = orderedTicketRepository.findByOrderId(orderId);
        cancelOrderedTicketsAndRestoreStock(orderedTickets);
    }

    private void cancelOrderedTicketsAndRestoreStock(List<OrderedTicket> orderedTickets) {
        orderedTickets.forEach(orderedTicket -> {
            orderedTicket.cancel();
            restoreStockInRedis(orderedTicket);
            sendStockRestoreEvent(orderedTicket);
        });
        orderedTicketRepository.saveAll(orderedTickets);
    }

    private void validateAndReduceStock(OrderRequestDto orderRequestDto) {
        for (OrderedTicketDto orderedTicketDto : orderRequestDto.getOrderedTickets()) {
            String stockKey = generateStockKey(orderedTicketDto.getTicketId());
            Integer stock = redisTemplate.opsForValue().get(stockKey);

            if (stock == null || stock < orderedTicketDto.getQuantity()) {
                throw new OrderBusinessException(OrderServiceErrorCode.INSUFFICIENT_STOCK);
            }

            redisTemplate.opsForValue().decrement(stockKey, orderedTicketDto.getQuantity());
        }
    }

    private String generateStockKey(Long ticketId) {
        return "stock:" + ticketId;
    }

    private int processOrderItems(Orders order, List<OrderedTicketDto> orderedTickets) {
        return orderedTickets.stream()
                .mapToInt(orderedTicketDto -> {
                    TicketDto ticket = fetchTicketDetails(orderedTicketDto.getTicketId());
                    validateTicket(ticket, orderedTicketDto.getQuantity());

                    OrderedTicket orderedTicket = createAndSaveOrderedTicket(order.getId(), ticket, orderedTicketDto.getQuantity());
                    return orderedTicket.getPrice();
                })
                .sum();
    }

    // Open Feign으로 티켓에서 정보 가져옴
    private TicketDto fetchTicketDetails(Long ticketId) {
        return ticketClient.getTicketById(ticketId);
    }

    private void validateTicket(TicketDto ticket, int requestedQuantity) {
        if (ticket == null || !"ON_SALE".equals(ticket.status()) || ticket.stock() < requestedQuantity) {
            throw new OrderBusinessException(OrderServiceErrorCode.INVALID_TICKET);
        }
    }

    private OrderedTicket createAndSaveOrderedTicket(Long orderId, TicketDto ticket, int quantity) {
        OrderedTicket orderedTicket = OrderedTicket.createPending(orderId, ticket.id(), quantity, ticket.price() * quantity);
        orderedTicketRepository.save(orderedTicket);
        return orderedTicket;
    }

    private void sendStockDecrementEvent(Orders order) {
        StockDecrEvent orderEvent = new StockDecrEvent(order.getId(), order.getUsername(), order.getTotalPrice());
        kafkaDecrTemplate.send(STOCK_DECREMENT_TOPIC, orderEvent); // 주문 생성 이벤트를 Kafka로 전송
    }


    //==================================================================================

    // 주문 취소 로직
    public void cancelOrder(String username, Long orderId) {
        Orders order = orderRepository.findByIdAndUsername(orderId, username)
                .orElseThrow(() -> new OrderBusinessException(OrderServiceErrorCode.ORDER_NOT_FOUND));

        order.cancelOrder();
        orderRepository.save(order);

        List<OrderedTicket> orderedTickets = orderedTicketRepository.findByOrderId(orderId);
        cancelOrderedTicketsAndRestoreStock(orderedTickets);
    }

    private void restoreStockInRedis(OrderedTicket orderedTicket) {
        String stockKey = generateStockKey(orderedTicket.getTicketId());
        redisTemplate.opsForValue().increment(stockKey, orderedTicket.getQuantity());
    }

    private void sendStockRestoreEvent(OrderedTicket orderedTicket) {
        StockIncrEvent stockRestoreEvent = new StockIncrEvent(
                orderedTicket.getTicketId(),
                orderedTicket.getQuantity()
        );
        kafkaIncrTemplate.send(STOCK_RESTORE_TOPIC, stockRestoreEvent); // 재고 복구 이벤트를 Kafka로 전송
    }

    // 결제 검증 시뮬레이션 메서드
    public PaymentResponse simulatePaymentVerification(String paymentKey) {
        // 결제 상태를 성공으로 시뮬레이션
        return new PaymentResponse(paymentKey, 10000, "COMPLETED"); // 예시 금액과 상태 설정
    }

    private void processRefund(double refundAmount) {
        // TODO : 환불 로직
    }
}