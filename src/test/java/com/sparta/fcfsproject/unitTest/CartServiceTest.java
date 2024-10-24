package com.sparta.fcfsproject.unitTest;

import com.sparta.fcfsproject.auth.entity.User;
import com.sparta.fcfsproject.common.exception.OrderBusinessException;
import com.sparta.fcfsproject.common.exception.OrderServiceErrorCode;
import com.sparta.fcfsproject.order.dto.CartItem;
import com.sparta.fcfsproject.order.dto.UpdateCartItemRequest;
import com.sparta.fcfsproject.order.repository.CartRepository;
import com.sparta.fcfsproject.order.service.CartService;
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

    private User user;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        user = new User(1L, "username", "email@example.com", "encodedPassword", "name", "010-1234-5678", "address", "ROLE_USER");
    }

    @Test
    @DisplayName("장바구니에 아이템 추가 시 신규 아이템이 추가된다.")
    void addItemToCart_NewItem() {
        CartItem cartItem = new CartItem(1L, "입장권", 1, 100); // ticketId, quantity, price

        when(cartRepository.getCartItem(user.getId(), cartItem.getTicketId())).thenReturn(null); // 기존 아이템 없음

        cartService.addItemToCart(user, cartItem);

        verify(cartRepository, times(1)).addItemToCart(user.getId(), cartItem);
    }

    @Test
    @DisplayName("장바구니에 아이템 추가 시 동일한 아이템이 있을 경우 수량이 업데이트된다.")
    void addItemToCart_ExistingItem() {
        CartItem existingItem = new CartItem(1L, "입장권", 1, 100); // ticketId, quantity, price
        CartItem newItem = new CartItem(1L, "입장권", 3, 300); // 동일한 ticketId, 새로운 quantity

        when(cartRepository.getCartItem(user.getId(), newItem.getTicketId())).thenReturn(existingItem); // 기존 아이템 있음

        cartService.addItemToCart(user, newItem);

        assertEquals(4, existingItem.getQuantity()); // 기존 수량 + 새 수량
        verify(cartRepository, times(1)).addItemToCart(user.getId(), existingItem);
    }

    @Test
    @DisplayName("장바구니 조회 시 비어 있지 않으면 정상적으로 반환된다.")
    void getCart_NotEmpty() {
        Map<String, CartItem> cart = new HashMap<>();
        CartItem cartItem = new CartItem(1L, "입장권", 1, 100);
        cart.put("ticket1", cartItem);

        when(cartRepository.getCart(user.getId())).thenReturn(cart);

        Map<String, CartItem> result = cartService.getCart(user);

        assertNotNull(result);
        assertEquals(1, result.size());
    }

    @Test
    @DisplayName("장바구니 조회 시 비어 있을 경우 예외 발생")
    void getCart_Empty() {
        when(cartRepository.getCart(user.getId())).thenReturn(new HashMap<>()); // 빈 장바구니

        OrderBusinessException exception = assertThrows(OrderBusinessException.class, () -> {
            cartService.getCart(user);
        });

        assertEquals(OrderServiceErrorCode.CART_NOT_FOUND, exception.getErrorCode());
    }

    @Test
    @DisplayName("장바구니 아이템 수량 수정")
    void updateItemQuantity() {
        Long ticketId = 1L;
        CartItem cartItem = new CartItem(ticketId, "입장권", 2, 100); // 초기 수량 2
        UpdateCartItemRequest updateRequest = new UpdateCartItemRequest(5); // 새로운 수량 5

        when(cartRepository.getCartItem(user.getId(), ticketId)).thenReturn(cartItem); // 아이템 존재

        cartService.updateItemQuantity(user, ticketId, updateRequest);

        assertEquals(5, cartItem.getQuantity()); // 수량 업데이트 확인
        verify(cartRepository, times(1)).addItemToCart(user.getId(), cartItem); // 저장 호출 확인
    }

    @Test
    @DisplayName("장바구니 아이템 삭제")
    void removeItemFromCart() {
        Long ticketId = 1L;
        CartItem cartItem = new CartItem(ticketId,"입장권", 1, 100); // 아이템 존재

        when(cartRepository.getCartItem(user.getId(), ticketId)).thenReturn(cartItem); // 아이템 존재

        cartService.removeItemFromCart(user, ticketId);

        verify(cartRepository, times(1)).removeItemFromCart(user.getId(), ticketId); // 삭제 호출 확인
    }

    @Test
    @DisplayName("장바구니 전체 삭제")
    void clearCart() {
        // 장바구니에 아이템이 있는 상태를 시뮬레이션
        Map<String, CartItem> cart = new HashMap<>();
        cart.put("item1", new CartItem(1L, "입장권", 1, 100)); // 티켓 ID, 수량, 가격 예시

        when(cartRepository.getCart(user.getId())).thenReturn(cart);
        cartService.clearCart(user);

        verify(cartRepository, times(1)).clearCart(user.getId()); // 장바구니 삭제 호출 확인
    }
}
