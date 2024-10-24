package com.sparta.fcfsproject.order.service;

import com.sparta.fcfsproject.auth.entity.User;
import com.sparta.fcfsproject.order.dto.CartItem;
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
        CartItem existingCartItem = cartRepository.getCartItem(user.getId(), cartItem.getTicketId());
        if (existingCartItem != null) {
            // 동일한 티켓이 있을 경우 수량 업데이트
            existingCartItem.setQuantity(existingCartItem.getQuantity() + cartItem.getQuantity());
            cartRepository.addItemToCart(user.getId(), existingCartItem); // 업데이트된 항목 저장
        } else {
            // 동일한 티켓이 없을 경우 새로 추가
            cartRepository.addItemToCart(user.getId(), cartItem);
        }
    }

    // 장바구니 조회
    public Map<String, CartItem> getCart(User user) {
        return cartRepository.getCart(user.getId());
    }

    // 장바구니 상품 수량 수정
    public void updateItemQuantity(User user, Long ticketId, Integer newQuantity) {
        CartItem cartItem = cartRepository.getCartItem(user.getId(), ticketId);
        if (cartItem != null) {
            cartItem.setQuantity(newQuantity);
            cartRepository.addItemToCart(user.getId(), cartItem); // 수량 수정 후 다시 저장
        }
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
