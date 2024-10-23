package com.sparta.fcfsproject.order.repository;

import com.sparta.fcfsproject.order.entity.CartItem;
import org.springframework.data.redis.core.HashOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Repository;

import java.util.Map;
import java.util.concurrent.TimeUnit;

@Repository
public class CartRepository {
    private static final String CART_PREFIX = "cart:";
    private final HashOperations<String, String, CartItem> hashOperations;
    private final RedisTemplate<String, Object> redisTemplate;

    public CartRepository(HashOperations<String, String, CartItem> hashOperations, RedisTemplate<String, Object> redisTemplate) {
        this.hashOperations = hashOperations;
        this.redisTemplate = redisTemplate;
    }

    // 장바구니에 상품 추가
    public void addItemToCart(Long userId, CartItem cartItem) {
        hashOperations.put(CART_PREFIX + userId, String.valueOf(cartItem.getTicketId()), cartItem);
        redisTemplate.expire(CART_PREFIX + userId, 3, TimeUnit.DAYS);
    }

    // 장바구니 조회
    public Map<String, CartItem> getCart(Long userId) {
        return hashOperations.entries(CART_PREFIX + userId);
    }

    // 장바구니 상품 수량 수정
    public void updateCartItemQuantity(Long userId, Long ticketId, Integer newQuantity) {
        CartItem cartItem = hashOperations.get(CART_PREFIX + userId, String.valueOf(ticketId));
        if (cartItem != null) {
            cartItem.setQuantity(newQuantity); // 수량 수정
            hashOperations.put(CART_PREFIX + userId, String.valueOf(ticketId), cartItem); // 수정된 장바구니 항목 저장
        }
    }

    // 장바구니에서 특정 상품 삭제
    public void removeItemFromCart(Long userId, Long ticketId) {
        hashOperations.delete(CART_PREFIX + userId, String.valueOf(ticketId));
    }

    // 장바구니 전체 삭제
    public void clearCart(Long userId) {
        hashOperations.delete(CART_PREFIX + userId);
    }
}

