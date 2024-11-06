package com.sparta.orderservice.service;


import com.sparta.orderservice.client.TicketClient;
import com.sparta.orderservice.dto.*;
import com.sparta.orderservice.entity.OrderedTicket;
import com.sparta.orderservice.entity.Orders;
import com.sparta.orderservice.event.StockDecrEvent;
import com.sparta.orderservice.event.StockIncrEvent;
import com.sparta.orderservice.exception.OrderBusinessException;
import com.sparta.orderservice.exception.OrderServiceErrorCode;
import com.sparta.orderservice.repository.OrderRepository;
import com.sparta.orderservice.repository.OrderedTicketRepository;

import com.sparta.orderservice.repository.PendingOrderRepository;
import com.sparta.orderservice.toss.PaymentResponse;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
public class OrderService {
    private final OrderRepository orderRepository;
    private final OrderConfirmationService orderConfirmationService;
    private final OrderedTicketRepository orderedTicketRepository;
    private final PendingOrderRepository pendingOrderRepository;
    private final TicketClient ticketClient;
    private final RefundService refundService;
    private final KafkaTemplate<String, StockDecrEvent> kafkaDecrTemplate;
    private final KafkaTemplate<String, StockIncrEvent> kafkaIncrTemplate;
    private final RedisTemplate<String, Integer> redisTemplate;
    private final RedisTemplate<String, Object> pendingOrderRedisTemplate;

    private static final String STOCK_DECREMENT_TOPIC = "stock-decrement-topic";
    private static final String STOCK_RESTORE_TOPIC = "stock-restore-topic";


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


    //========================== 결제 화면 진입 ==========================================

    public List<String> createPendingOrder(String username, OrderRequestDto orderRequestDto) {
        List<String> pendingOrderIds = new ArrayList<>();

        for (OrderedTicketDto orderedTicket : orderRequestDto.getOrderedTickets()) {
            String uuid = "" + UUID.randomUUID();

            String stockKey = "stock:" + orderedTicket.getTicketId();   // 전체 재고
            String reservedStockKey = "reserved_stock:" + orderedTicket.getTicketId();  // 예약 재고

            Integer totalStock = redisTemplate.opsForValue().get(stockKey);
            Integer reservedStock = redisTemplate.opsForValue().get(reservedStockKey);

            int availableStock = (totalStock != null ? totalStock : 0) - (reservedStock != null ? reservedStock : 0);

            if (availableStock < orderedTicket.getQuantity()) {
                throw new OrderBusinessException(OrderServiceErrorCode.INSUFFICIENT_STOCK);
            }

            PendingOrder pendingOrder = PendingOrder.builder()
                    .username(username)
                    .ticketId(orderedTicket.getTicketId())
                    .quantity(orderedTicket.getQuantity())
                    .createdAt(LocalDateTime.now())
                    .status(PendingOrder.PendingStatus.PENDING)
                    .build();

            pendingOrderRepository.savePendingOrder(uuid, pendingOrder);

            createReservation(uuid, username, orderedTicket.getQuantity());
            pendingOrderIds.add(uuid);
        }
        return pendingOrderIds; // 예비 주문 ID 반환
    }

    public void createReservation(String id, String username, int quantity) {
        String reservedStockKey = "reserved_stock:" + id + ":" + username;

        redisTemplate.opsForValue().set(reservedStockKey, quantity);
        redisTemplate.expire(reservedStockKey, 10, TimeUnit.MINUTES);
    }

    // ========================= 결제 시도 (결제화면에서 결제하기 버튼 누름) ================================

    public void attemptPayment(String username, List<String> pendingOrderIds) {
        List<PendingOrder> pendingOrders = new ArrayList<>();
        List<String> reservedStockKeys = new ArrayList<>();

        for (String pendingOrderId : pendingOrderIds) {
            PendingOrder pendingOrder = pendingOrderRepository.getPendingOrder(pendingOrderId);
            if (pendingOrder == null || pendingOrder.getStatus() != PendingOrder.PendingStatus.PENDING) {
                throw new OrderBusinessException(OrderServiceErrorCode.INVALID_PENDING_ORDER_STATUS);
            }

            // 예약된 재고 확인
            String reservedStockKey = "reserved_stock:" + pendingOrderId + ":" + username;
            if (redisTemplate.opsForValue().get(reservedStockKey) == null) {
                throw new OrderBusinessException(OrderServiceErrorCode.INVALID_PENDING_STOCK);
            }

            pendingOrders.add(pendingOrder);
            reservedStockKeys.add(reservedStockKey);
        }
        try {
            // 결제 모듈 가정하여 시뮬레이션
            String paymentKey = UUID.randomUUID().toString();
            PaymentResponse paymentResponse = simulatePaymentVerification(paymentKey);

            // 결제와 주문 확정 로직(트랜잭션 설정)
            orderConfirmationService.confirmOrderAndPayment(paymentResponse, pendingOrders);

        } catch (Exception e) {
            // 결제 실패 시 Pending 테이블 삭제, 예약키 삭제
            pendingOrderIds.forEach(pendingOrderRepository::deletePendingOrder);
            reservedStockKeys.forEach(redisTemplate::delete);
            throw new OrderBusinessException(OrderServiceErrorCode.PAYMENT_FAILED);
        }
    }

    // 결제 검증 시뮬레이션 메서드
    public PaymentResponse simulatePaymentVerification(String paymentKey) {
        // 결제 상태를 성공으로 시뮬레이션
        return new PaymentResponse(paymentKey, 10000, "COMPLETED"); // 예시 금액과 상태 설정
    }


    //==================================================================================
    // 주문 취소 로직
    @Transactional
    public void cancelOrder(String username, Long orderId) {
        Orders order = orderRepository.findByIdAndUsername(orderId, username)
                .orElseThrow(() -> new OrderBusinessException(OrderServiceErrorCode.ORDER_NOT_FOUND));

        order.cancelOrder();
        orderRepository.save(order);

        List<OrderedTicket> orderedTickets = orderedTicketRepository.findByOrderId(orderId);
        orderedTickets.forEach(orderedTicket -> {
            orderedTicket.cancel();
            restoreStockInRedis(orderedTicket);
            sendStockRestoreEvent(orderedTicket);
        });
        orderedTicketRepository.saveAll(orderedTickets);
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

    private String generateStockKey(Long ticketId) {
        return "stock:" + ticketId;
    }
}