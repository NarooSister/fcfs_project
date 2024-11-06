package com.sparta.orderservice.repository;

import com.sparta.orderservice.dto.PendingOrder;
import org.springframework.data.redis.core.HashOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Repository;

import java.util.Objects;
import java.util.concurrent.TimeUnit;

@Repository
public class PendingOrderRepository {
    private static final String PENDING_ORDER_PREFIX = "pending_order:";
    private final HashOperations<String, String, String> hashOperations;
    private final RedisTemplate<String, Object> redisTemplate;

    public PendingOrderRepository(HashOperations<String, String, String> hashOperations,
                                  RedisTemplate<String, Object> redisTemplate) {
        this.hashOperations = hashOperations;
        this.redisTemplate = redisTemplate;
    }

    // 예비 주문 추가 (10분 뒤 만료)
    public void savePendingOrder(String orderId, PendingOrder pendingOrder) {
        String key = PENDING_ORDER_PREFIX + orderId;

        hashOperations.put(key, "username", pendingOrder.getUsername());
        hashOperations.put(key, "ticketId", String.valueOf(pendingOrder.getTicketId()));
        hashOperations.put(key, "quantity", String.valueOf(pendingOrder.getQuantity()));
        hashOperations.put(key, "status", pendingOrder.getStatus().name());

        // 만료 시간 설정
        redisTemplate.expire(key, 10, TimeUnit.MINUTES);
    }

    // 예비 주문 조회
    public PendingOrder getPendingOrder(String orderId) {
        String key = PENDING_ORDER_PREFIX + orderId;

        // 해시 필드를 읽어와 객체로 생성
        String username = hashOperations.get(key, "username");
        Long ticketId = Long.valueOf(Objects.requireNonNull(hashOperations.get(key, "ticketId")));
        Integer quantity = Integer.valueOf(Objects.requireNonNull(hashOperations.get(key, "quantity")));
        PendingOrder.PendingStatus status = PendingOrder.PendingStatus.valueOf(hashOperations.get(key, "status"));

        return PendingOrder.builder()
                .username(username)
                .ticketId(ticketId)
                .quantity(quantity)
                .status(status)
                .build();
    }

    // 예비 주문 상태 업데이트
    public void updateOrderStatus(String orderId, PendingOrder.PendingStatus status) {
        String key = PENDING_ORDER_PREFIX + orderId;
        hashOperations.put(key, "status", status.name());
    }

    // 예비 주문 삭제
    public void deletePendingOrder(String orderId) {
        String key = PENDING_ORDER_PREFIX + orderId;
        redisTemplate.delete(key);
    }
}
