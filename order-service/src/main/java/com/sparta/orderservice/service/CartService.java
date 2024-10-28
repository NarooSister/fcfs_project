package com.sparta.orderservice.service;

import com.sparta.orderservice.dto.CartItem;
import com.sparta.orderservice.dto.UpdateCartItemRequest;
import com.sparta.orderservice.exception.OrderBusinessException;
import com.sparta.orderservice.exception.OrderServiceErrorCode;
import com.sparta.orderservice.repository.CartRepository;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
public class CartService {

    private final CartRepository cartRepository;

    public CartService(CartRepository cartRepository) {
        this.cartRepository = cartRepository;
    }

    // 장바구니에 아이템 추가
    public void addItemToCart(String username, CartItem cartItem) {
        CartItem existingCartItem = cartRepository.getCartItem(username, cartItem.getTicketId());
        if (existingCartItem != null) {
            // 동일한 티켓이 있을 경우 수량 업데이트
            existingCartItem.setQuantity(existingCartItem.getQuantity() + cartItem.getQuantity());
            cartRepository.addItemToCart(username, existingCartItem); // 업데이트된 항목 저장
        } else {
            // 동일한 티켓이 없을 경우 새로 추가
            cartRepository.addItemToCart(username, cartItem);
        }
    }

    // 장바구니 조회
    public Map<String, CartItem> getCart(String username) {
        // 장바구니가 비어있지 않은지 확인하고 반환
        return validateCartNotEmpty(username);
    }

    // 장바구니 상품 수량 수정
    public void updateItemQuantity(String username, Long ticketId, UpdateCartItemRequest updateCartItemRequest) {
        CartItem cartItem = validateCartItemExists(username, ticketId); // 해당 상품이 존재하는지 확인
        cartItem.setQuantity(updateCartItemRequest.getNewQuantity());
        cartRepository.addItemToCart(username, cartItem); // 수량 수정 후 다시 저장
    }

    // 장바구니 아이템 삭제
    public void removeItemFromCart(String username, Long ticketId) {
        validateCartItemExists(username, ticketId); // 해당 상품이 존재하는지 확인
        cartRepository.removeItemFromCart(username, ticketId);
    }

    // 장바구니 전체 삭제
    public void clearCart(String username) {
        validateCartNotEmpty(username);
        cartRepository.clearCart(username);
    }

    private CartItem validateCartItemExists(String username, Long ticketId) {
        CartItem cartItem = cartRepository.getCartItem(username, ticketId);
        if (cartItem == null) {
            throw new OrderBusinessException(OrderServiceErrorCode.CART_NOT_FOUND_TICKET);
        }
        return cartItem;
    }
    private Map<String, CartItem> validateCartNotEmpty(String username) {
        Map<String, CartItem> cart = cartRepository.getCart(username);
        if (cart.isEmpty()) {
            throw new OrderBusinessException(OrderServiceErrorCode.CART_NOT_FOUND);
        }
        return cart;
    }
}
