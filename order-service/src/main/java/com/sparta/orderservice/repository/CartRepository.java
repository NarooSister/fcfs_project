package com.sparta.orderservice.repository;

import com.sparta.orderservice.dto.CartItem;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.HashOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Repository;

import java.util.Map;
import java.util.concurrent.TimeUnit;

@Repository
@Slf4j
public class CartRepository {
    private static final String CART_PREFIX = "cart:";
    private final HashOperations<String, String, CartItem> hashOperations;
    private final RedisTemplate<String, Object> redisTemplate;

    public CartRepository(HashOperations<String, String, CartItem> hashOperations, RedisTemplate<String, Object> redisTemplate) {
        this.hashOperations = hashOperations;
        this.redisTemplate = redisTemplate;
    }

    // 장바구니에 상품 추가 (3일 뒤 만료)
    public void addItemToCart(String username, CartItem cartItem) {
        hashOperations.put(CART_PREFIX + username, String.valueOf(cartItem.getTicketId()), cartItem);
        redisTemplate.expire(CART_PREFIX + username, 3, TimeUnit.DAYS);
    }

    // 장바구니 조회
    public Map<String, CartItem> getCart(String username) {
        return hashOperations.entries(CART_PREFIX + username);
    }

    // 장바구니 특정 티켓 조회
    public CartItem getCartItem(String username, Long ticketId) {
        return hashOperations.get(CART_PREFIX + username, String.valueOf(ticketId));
    }

    // 장바구니에서 특정 상품 삭제
    public void removeItemFromCart(String username, Long ticketId) {
        hashOperations.delete(CART_PREFIX + username, String.valueOf(ticketId));
    }

    // 장바구니 전체 삭제
    public void clearCart(String username) {
        redisTemplate.delete(CART_PREFIX + username);
    }
}

