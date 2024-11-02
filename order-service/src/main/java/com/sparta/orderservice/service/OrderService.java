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

import jakarta.transaction.Transactional;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class OrderService {
    private final OrderRepository orderRepository;
    private final OrderedTicketRepository orderedTicketRepository;
    private final TicketClient ticketClient;
    private final RefundService refundService;
    private final KafkaTemplate<String, StockDecrEvent> kafkaDecrTemplate;
    private final KafkaTemplate<String, StockIncrEvent> kafkaIncrTemplate;
    private final RedisTemplate<String, Integer> redisTemplate;

    public OrderService(OrderRepository orderRepository, OrderedTicketRepository orderedTicketRepository, RefundService refundService, TicketClient ticketClient, KafkaTemplate<String, StockDecrEvent> kafkaDecrTemplate, KafkaTemplate<String, StockIncrEvent> kafkaIncrTemplate, RedisTemplate<String, Integer> redisTemplate) {
        this.orderRepository = orderRepository;
        this.orderedTicketRepository = orderedTicketRepository;
        this.refundService = refundService;
        this.ticketClient = ticketClient;
        this.kafkaDecrTemplate = kafkaDecrTemplate;
        this.kafkaIncrTemplate = kafkaIncrTemplate;
        this.redisTemplate = redisTemplate;
    }

//    public void sendMessage(String topic, String key, String message) {
//        for (int i = 0; i < 10; i++) {
//            kafkaTemplate.send(topic, key, message + " " + i);
//        }
//    }

    // 사용자의 모든 주문 가져오기
    public List<OrderDto> readAllOrders(String username) {
        List<Orders> orderList = orderRepository.findAllByUsername(username);
        if (orderList.isEmpty()) {
            throw new OrderBusinessException(OrderServiceErrorCode.ALL_ORDER_NOT_FOUND);
        }
        return orderList.stream()
                .map(OrderDto::new)
                .collect(Collectors.toList());
    }

    // 사용자의 주문 상세 내역 가져오기
    public OrderDto readOrder(String username, Long orderId) {
        Orders order = orderRepository.findByIdAndUsername(orderId, username)
                .orElseThrow(() -> new OrderBusinessException(OrderServiceErrorCode.ORDER_NOT_FOUND)
                );
        return new OrderDto(order);
    }

    // 기본 주문 생성 구현 (TODO : 주문 생성 나중에 추가)
    @Transactional
    public void createOrder(String username, OrderRequestDto orderRequestDto) {
        // 주문 생성
        Orders order = new Orders(username);
        orderRepository.save(order);

        // Redis에서 재고 확인 및 차감
        for (OrderedTicketDto orderedTicketDto : orderRequestDto.getOrderedTickets()) {
            String stockKey = "stock:" + orderedTicketDto.getTicketId();
            Integer stock = redisTemplate.opsForValue().get(stockKey);

            if (stock == null || stock < orderedTicketDto.getQuantity()) {
                throw new OrderBusinessException(OrderServiceErrorCode.INSUFFICIENT_STOCK);
            }

            // Redis에서 재고 차감
            redisTemplate.opsForValue().decrement(stockKey, orderedTicketDto.getQuantity());
        }

        // 주문 항목 및 총 금액 계산
        int totalPrice = processOrderItems(order, orderRequestDto.getOrderedTickets());

        // 주문 최종 금액 업데이트
        order.updateTotalPrice(totalPrice);
        orderRepository.save(order);


        // 재고 차감 이벤트 전송
        sendStockDecrementEvent(order);

        // 최종적으로 주문 생성 완료
        // TODO : 결제 프로세스로 연결
    }

    private int processOrderItems(Orders order, List<OrderedTicketDto> orderedTickets) {
        int totalPrice = 0;

        for (OrderedTicketDto orderedTicketDto : orderedTickets) {
            // 티켓 정보 가져오기 및 검증
            TicketDto ticket = ticketClient.getTicketById(orderedTicketDto.getTicketId());
            validateTicket(ticket, orderedTicketDto.getQuantity());

            // OrderedTicket 생성 및 저장
            OrderedTicket orderedTicket = createOrderedTicket(order.getId(), ticket, orderedTicketDto.getQuantity());
            orderedTicketRepository.save(orderedTicket);

            // 총 가격 업데이트
            totalPrice += orderedTicket.getPrice();
        }

        return totalPrice;
    }
    private void validateTicket(TicketDto ticket, int requestedQuantity) {
        if (ticket == null) {
            throw new OrderBusinessException(OrderServiceErrorCode.TICKET_NOT_FOUND);
        }
        if (!"ON_SALE".equals(ticket.status())) {
            throw new OrderBusinessException(OrderServiceErrorCode.TICKET_NOT_ON_SALE);
        }
        if (ticket.stock() < requestedQuantity) {
            throw new OrderBusinessException(OrderServiceErrorCode.INSUFFICIENT_STOCK);
        }
    }

    private OrderedTicket createOrderedTicket(Long orderId, TicketDto ticket, int quantity) {
        return OrderedTicket.createPending(
                orderId,
                ticket.id(),
                quantity,
                ticket.price() * quantity
        );
    }

    private void sendStockDecrementEvent(Orders order) {
        StockDecrEvent orderEvent = new StockDecrEvent(order.getId(), order.getUsername(), order.getTotalPrice());
        kafkaDecrTemplate.send("stock-decrement-topic", orderEvent); // 주문 생성 이벤트를 Kafka로 전송
    }

    // 주문 취소 로직
    public void cancelOrder(String username, Long orderId) {
        // 사용자와 주문 아이디로 주문 찾기
        Orders order = orderRepository.findByIdAndUsername(orderId, username)
                .orElseThrow(() -> new OrderBusinessException(OrderServiceErrorCode.ORDER_NOT_FOUND));

        List<OrderedTicket> orderedTickets = orderedTicketRepository.findByOrderId(orderId);

        for (OrderedTicket orderedTicket : orderedTickets) {
            // 티켓 가져오기
            TicketDto ticket = ticketClient.getTicketById(orderedTicket.getTicketId());
            if (ticket == null) {
                throw new OrderBusinessException(OrderServiceErrorCode.TICKET_NOT_FOUND);
            }

            // 티켓 관람일 기준으로 환불 금액 계산
            double refundAmount = refundService.calculateRefund(ticket, orderedTicket);

            // 환불 처리 (결제 시스템)
            processRefund(refundAmount);

            // 티켓 취소 처리
            orderedTicket.cancel();

            // Redis에서 재고 복구
            String stockKey = "stock:" + orderedTicket.getTicketId();
            redisTemplate.opsForValue().increment(stockKey, orderedTicket.getQuantity());

            // Kafka를 통해 재고 복구 이벤트 전송
            sendStockRestoreEvent(orderedTicket);
        }
        orderedTicketRepository.saveAll(orderedTickets);
        orderRepository.save(order);
    }

    private void sendStockRestoreEvent(OrderedTicket orderedTicket) {
        StockIncrEvent stockRestoreEvent = new StockIncrEvent(
                orderedTicket.getTicketId(),
                orderedTicket.getQuantity()
        );
        kafkaIncrTemplate.send("stock-restore-topic", stockRestoreEvent); // 재고 복구 이벤트를 Kafka로 전송
    }
    private void processRefund(double refundAmount) {
        // TODO : 환불 로직
    }

    public void reduceStock(Orders order) {
        // TODO : 재고 감소 로직
    }

    public void restoreStock(Orders order) {
        // TODO : 재고 복구 로직
    }
}