package com.sparta.orderservice.unit_test;

import com.sparta.orderservice.dto.CartItem;
import com.sparta.orderservice.dto.UpdateCartItemRequest;
import com.sparta.orderservice.exception.OrderBusinessException;
import com.sparta.orderservice.exception.OrderServiceErrorCode;
import com.sparta.orderservice.repository.CartRepository;
import com.sparta.orderservice.service.CartService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class CartServiceTest {
    @Mock
    private CartRepository cartRepository;

    @InjectMocks
    private CartService cartService;

    private String username;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        username = "username";
    }

    @Test
    @DisplayName("장바구니에 아이템 추가 시 신규 아이템이 추가된다.")
    void addItemToCart_NewItem() {
        CartItem cartItem = new CartItem(1L, "입장권", 1, 100);

        when(cartRepository.getCartItem(username, cartItem.getTicketId())).thenReturn(null);

        cartService.addItemToCart(username, cartItem);

        verify(cartRepository, times(1)).addItemToCart(username, cartItem);
    }

    @Test
    @DisplayName("장바구니에 아이템 추가 시 동일한 아이템이 있을 경우 수량이 업데이트된다.")
    void addItemToCart_ExistingItem() {
        CartItem existingItem = new CartItem(1L, "입장권", 1, 100);
        CartItem newItem = new CartItem(1L, "입장권", 3, 300);

        when(cartRepository.getCartItem(username, newItem.getTicketId())).thenReturn(existingItem);

        cartService.addItemToCart(username, newItem);

        assertEquals(4, existingItem.getQuantity());
        verify(cartRepository, times(1)).addItemToCart(username, existingItem);
    }

    @Test
    @DisplayName("장바구니 조회 시 비어 있지 않으면 정상적으로 반환된다.")
    void getCart_NotEmpty() {
        Map<String, CartItem> cart = new HashMap<>();
        CartItem cartItem = new CartItem(1L, "입장권", 1, 100);
        cart.put("ticket1", cartItem);

        when(cartRepository.getCart(username)).thenReturn(cart);

        Map<String, CartItem> result = cartService.getCart(username);

        assertNotNull(result);
        assertEquals(1, result.size());
    }

    @Test
    @DisplayName("장바구니 조회 시 비어 있을 경우 예외 발생")
    void getCart_Empty() {
        when(cartRepository.getCart(username)).thenReturn(new HashMap<>());

        OrderBusinessException exception = assertThrows(OrderBusinessException.class, () -> {
            cartService.getCart(username);
        });

        assertEquals(OrderServiceErrorCode.CART_NOT_FOUND, exception.getErrorCode());
    }

    @Test
    @DisplayName("장바구니 아이템 수량 수정")
    void updateItemQuantity() {
        Long ticketId = 1L;
        CartItem cartItem = new CartItem(ticketId, "입장권", 2, 100);
        UpdateCartItemRequest updateRequest = new UpdateCartItemRequest(5);

        when(cartRepository.getCartItem(username, ticketId)).thenReturn(cartItem);

        cartService.updateItemQuantity(username, ticketId, updateRequest);

        assertEquals(5, cartItem.getQuantity());
        verify(cartRepository, times(1)).addItemToCart(username, cartItem);
    }

    @Test
    @DisplayName("장바구니 아이템 삭제")
    void removeItemFromCart() {
        Long ticketId = 1L;
        CartItem cartItem = new CartItem(ticketId, "입장권", 1, 100);

        when(cartRepository.getCartItem(username, ticketId)).thenReturn(cartItem);

        cartService.removeItemFromCart(username, ticketId);

        verify(cartRepository, times(1)).removeItemFromCart(username, ticketId);
    }

    @Test
    @DisplayName("장바구니 전체 삭제")
    void clearCart() {
        Map<String, CartItem> cart = new HashMap<>();
        cart.put("item1", new CartItem(1L, "입장권", 1, 100));

        when(cartRepository.getCart(username)).thenReturn(cart);
        cartService.clearCart(username);

        verify(cartRepository, times(1)).clearCart(username);
    }
}

