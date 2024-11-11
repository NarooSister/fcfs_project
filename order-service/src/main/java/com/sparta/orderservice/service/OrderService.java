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
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.data.redis.core.script.RedisScript;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
@Slf4j
public class OrderService {
    private final OrderRepository orderRepository;
    private final OrderConfirmationService orderConfirmationService;
    private final OrderedTicketRepository orderedTicketRepository;
    private final PendingOrderRepository pendingOrderRepository;
    private final TicketClient ticketClient;
    private final RefundService refundService;
    private final KafkaTemplate<String, StockIncrEvent> kafkaIncrTemplate;
    private final RedisTemplate<String, Integer> redisTemplate;
    private final RedisTemplate<String, Object> pendingOrderRedisTemplate;

    private static final String STOCK_RESTORE_TOPIC = "stock-restore-topic";

    private static final String CHECK_AND_RESERVE_STOCK_SCRIPT = """
            -- 전체 재고를 가져옵니다.
            local stock = tonumber(redis.call('GET', KEYS[1]))
            
            -- 모든 예약된 재고의 합계를 계산합니다.
            local totalReserved = 0
            for i, key in ipairs(redis.call('KEYS', ARGV[2])) do
                totalReserved = totalReserved + tonumber(redis.call('GET', key))
            end
            
            -- 요청된 수량을 가져옵니다.
            local requestedQuantity = tonumber(ARGV[1])
            
            -- 사용 가능한 재고를 계산합니다.
            local availableStock = stock - totalReserved
            
            -- 사용 가능한 재고가 요청된 수량보다 크거나 같으면 예약을 설정하고, 그렇지 않으면 0 반환
            if availableStock >= requestedQuantity then
                redis.call('SET', KEYS[2], requestedQuantity)
                redis.call('EXPIRE', KEYS[2], 600)
                return 1
            else
                return 0
            end
            """;


    // 사용자의 모든 주문 가져오기
    public List<OrderDto> readAllOrdersByUser(String username) {
        return Optional.ofNullable(orderRepository.findAllByUsername(username)).filter(orderList -> !orderList.isEmpty()).orElseThrow(() -> new OrderBusinessException(OrderServiceErrorCode.ALL_ORDER_NOT_FOUND)).stream().map(OrderDto::new).toList();
    }

    // 사용자의 주문 상세 내역 가져오기
    public OrderDto readOrderByUser(String username, Long orderId) {
        return orderRepository.findByIdAndUsername(orderId, username).map(OrderDto::new).orElseThrow(() -> new OrderBusinessException(OrderServiceErrorCode.ORDER_NOT_FOUND));
    }

    //========================== 결제 화면 진입 ==========================================

    public List<String> createPendingOrder(String username, OrderRequestDto orderRequestDto) {
        List<String> pendingOrderIds = new ArrayList<>();

        if (orderRequestDto == null || orderRequestDto.getOrderedTickets().isEmpty()) {
            throw new OrderBusinessException(OrderServiceErrorCode.INVALID_ORDER_REQUEST);
        }
        List<Long> ticketIds = orderRequestDto.getOrderedTickets().stream().map(OrderedTicketDto::getTicketId).toList();
        Map<Long, Integer> ticketPrices = ticketClient.getTicketPrices(ticketIds);

        for (OrderedTicketDto orderedTicket : orderRequestDto.getOrderedTickets()) {
            if (orderedTicket.getTicketId() == null || orderedTicket.getQuantity() == null || orderedTicket.getQuantity() <= 0) {
                throw new OrderBusinessException(OrderServiceErrorCode.PRICE_MISMATCH);
            }

            // 서버의 실제 가격 정보 확인 (예: TicketClient를 통해 원본 데이터 가져오기)
            Integer ticketPrice = ticketPrices.get(orderedTicket.getTicketId());
            Integer totalPrice = ticketPrice * orderedTicket.getQuantity();
            if (!orderedTicket.getPrice().equals(totalPrice)) {
                throw new OrderBusinessException(OrderServiceErrorCode.PRICE_MISMATCH);
            }

            String stockKey = generateStockKey(orderedTicket.getTicketId());
            String reservedStockKey = "reserved_stock:" + orderedTicket.getTicketId() + ":*";

//            // Redis에 null 값이 들어가지 않도록 0 기본값 적용
//            Integer totalStock = redisTemplate.opsForValue().get(stockKey);
//            if (totalStock == null) totalStock = 0;
//
//            Integer quantity = orderedTicket.getQuantity();
//            if (quantity == null) quantity = 0;

            // Lua 스크립트 실행을 위한 RedisScript 객체 생성
            RedisScript<Long> script = new DefaultRedisScript<>(CHECK_AND_RESERVE_STOCK_SCRIPT, Long.class);

            Long result = redisTemplate.execute(script, Arrays.asList(stockKey, reservedStockKey), orderedTicket.getQuantity().toString(), "reserved_stock:" + orderedTicket.getTicketId() + ":*");

            if (result == null || result == 0) {
                throw new OrderBusinessException(OrderServiceErrorCode.INSUFFICIENT_STOCK);
            }
            String uuid = UUID.randomUUID().toString();

            PendingOrder pendingOrder = PendingOrder.builder().username(username).ticketId(orderedTicket.getTicketId()).price(totalPrice).quantity(orderedTicket.getQuantity()).createdAt(LocalDateTime.now()).status(PendingOrder.PendingStatus.PENDING).build();

            pendingOrderRepository.savePendingOrder(uuid, pendingOrder);
            pendingOrderIds.add(uuid);
        }
        return pendingOrderIds; // 예비 주문 ID 반환
    }


    // ========================= 결제 시도 (결제화면에서 결제하기 버튼 누름) ================================

    public void attemptPayment(String username, List<String> pendingOrderIds) {
        List<PendingOrder> pendingOrders = new ArrayList<>();
        List<String> reservedStockKeys = new ArrayList<>();
        int totalAmount = 0;

        for (String pendingOrderId : pendingOrderIds) {
            PendingOrder pendingOrder = pendingOrderRepository.getPendingOrder(pendingOrderId);
            if (pendingOrder == null || pendingOrder.getStatus() != PendingOrder.PendingStatus.PENDING) {
                throw new OrderBusinessException(OrderServiceErrorCode.INVALID_PENDING_ORDER_STATUS);
            }

            // 예약된 재고 확인
            String reservedStockKey = generateReservedStockKey(pendingOrderId, username);
            if (redisTemplate.opsForValue().get(reservedStockKey) == null) {
                throw new OrderBusinessException(OrderServiceErrorCode.INVALID_PENDING_STOCK);
            }

            totalAmount += pendingOrder.getPrice();
            pendingOrders.add(pendingOrder);
            reservedStockKeys.add(reservedStockKey);
        }

        try {
            // 결제 모듈 가정하여 시뮬레이션
            String paymentKey = UUID.randomUUID().toString();
            PaymentResponse paymentResponse = simulatePaymentVerification(paymentKey, totalAmount);

            // 결제 성공 시 실제 재고 감소
            for (PendingOrder pendingOrder : pendingOrders) {
                String stockKey = generateStockKey(pendingOrder.getTicketId());
                redisTemplate.opsForValue().decrement(stockKey, pendingOrder.getQuantity());
            }

            // 결제와 주문 확정 로직, 카프카 재고감소 이벤트 실행(트랜잭션 설정)
            orderConfirmationService.confirmOrderAndPayment(paymentResponse, pendingOrders);

        } catch (Exception e) {
            // 결제 실패 시 Pending 테이블 삭제, 예약키 삭제
            pendingOrderIds.forEach(pendingOrderRepository::deletePendingOrder);
            reservedStockKeys.forEach(redisTemplate::delete);
            throw new OrderBusinessException(OrderServiceErrorCode.PAYMENT_FAILED);
        }
    }

    // 결제 검증 시뮬레이션 메서드
    public PaymentResponse simulatePaymentVerification(String paymentKey, Integer totalAmount) {
        // 결제 상태를 성공으로 시뮬레이션
        return new PaymentResponse(paymentKey, totalAmount, "COMPLETED"); // 예시 금액과 상태 설정
    }

    private String generateReservedStockKey(String ticketUUID, String username) {
        return "reserved_stock:" + ticketUUID + ":" + username;
    }

    //==================================================================================
    // 주문 취소 로직
    @Transactional
    public void cancelOrder(String username, Long orderId) {
        Orders order = orderRepository.findByIdAndUsername(orderId, username).orElseThrow(() -> new OrderBusinessException(OrderServiceErrorCode.ORDER_NOT_FOUND));

        order.cancelOrder();
        orderRepository.save(order);

        List<OrderedTicket> orderedTickets = orderedTicketRepository.findByOrderId(orderId);

        // 각 티켓 ID 수집
        List<Long> ticketIds = orderedTickets.stream().map(OrderedTicket::getTicketId).toList();

        // TicketClient를 사용해 티켓 정보 일괄 가져오기
        Map<Long, TicketDto> ticketInfoMap = ticketClient.getTickets(ticketIds);

        orderedTickets.forEach(orderedTicket -> {
            TicketDto ticketDto = ticketInfoMap.get(orderedTicket.getTicketId());

            if (ticketDto != null) {
                double refundAmount = refundService.calculateRefund(ticketDto, orderedTicket);
                processRefund(refundAmount, orderedTicket);
            }

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
        StockIncrEvent stockRestoreEvent = new StockIncrEvent(orderedTicket.getTicketId(), orderedTicket.getQuantity());
        kafkaIncrTemplate.send(STOCK_RESTORE_TOPIC, stockRestoreEvent); // 재고 복구 이벤트를 Kafka로 전송
    }

    private String generateStockKey(Long ticketId) {
        return "stock:" + ticketId;
    }

    private void processRefund(double refundAmount, OrderedTicket orderedTicket) {
        // 실제 환불 처리 로직 구현 예시
        log.info("Ticket ID: {}, 환불 금액: {}", orderedTicket.getId(), refundAmount);
    }

    //========================재고 확인 api==========================

    // 현재 사용 가능한 재고 조회 (모든 예약 재고 제외)
    public int getCurrentStock(Long ticketId) {
        String stockKey = generateStockKey(ticketId);
        Integer totalStock = redisTemplate.opsForValue().get(stockKey);

        int totalReservedStock = getTotalReservedStock(ticketId);

        // 현재 재고에서 모든 예약된 재고를 제외
        int availableStock = (totalStock != null ? totalStock : 0) - totalReservedStock;
        return Math.max(availableStock, 0); // 조회 api 이므로 제대로 되지 않은 값이면 무조건 0 반환(예외x)
    }

    private int getTotalReservedStock(Long ticketId) {
        // 예약 재고 키 패턴 (ex: "reserved_stock:ticketId:*")
        String reservedStockPattern = "reserved_stock:" + ticketId + ":*";

        // 모든 예약된 재고를 조회하고 합산
        return redisTemplate.keys(reservedStockPattern).stream().map(key -> redisTemplate.opsForValue().get(key)).filter(Objects::nonNull).mapToInt(Integer::intValue).sum();
    }

}