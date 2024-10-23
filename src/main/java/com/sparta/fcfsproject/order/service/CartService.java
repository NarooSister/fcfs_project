package com.sparta.fcfsproject.order.service;

import com.sparta.fcfsproject.auth.entity.User;
import com.sparta.fcfsproject.order.entity.CartItem;
import com.sparta.fcfsproject.order.repository.CartRepository;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
public class CartService {

    private final CartRepository cartRepository;

    public CartService(CartRepository cartRepository) {
        this.cartRepository = cartRepository;
    }

    // 장바구니에 아이템 추가
    public void addItemToCart(User user, CartItem cartItem) {
        cartRepository.addItemToCart(user.getId(), cartItem);
    }

    // 장바구니 조회
    public Map<String, CartItem> getCart(User user) {
        return cartRepository.getCart(user.getId());
    }

    // 장바구니 상품 수량 수정
    public void updateItemQuantity(User user, Long ticketId, Integer newQuantity) {
        cartRepository.updateCartItemQuantity(user.getId(), ticketId, newQuantity);
    }

    // 장바구니 아이템 삭제
    public void removeItemFromCart(User user, Long ticketId) {
        cartRepository.removeItemFromCart(user.getId(), ticketId);
    }

    // 장바구니 전체 삭제
    public void clearCart(User user) {
        cartRepository.clearCart(user.getId());
    }
}
