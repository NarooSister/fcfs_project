package com.sparta.orderservice.controller;

import com.sparta.orderservice.dto.CartItem;
import com.sparta.orderservice.dto.UpdateCartItemRequest;
import com.sparta.orderservice.dto.UserInfo;
import com.sparta.orderservice.service.CartService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/cart")
public class CartController {
    private final CartService cartService;

    public CartController(CartService cartService) {
        this.cartService = cartService;
    }

    // 장바구니에 아이템 추가
    @PostMapping
    public ResponseEntity<String> addToCart(@Valid @RequestBody CartItem cartItem, UserInfo userInfo) {
        cartService.addItemToCart(userInfo.username(), cartItem);
        return ResponseEntity.ok("장바구니에 아이템이 성공적으로 추가되었습니다.");
    }

    // 장바구니 조회
    @GetMapping
    public ResponseEntity<Map<String, CartItem>> getCartItems(UserInfo userInfo) {
        Map<String, CartItem> cartItems = cartService.getCart(userInfo.username());
        return ResponseEntity.ok(cartItems);
    }

    // 장바구니 수량 수정
    @PatchMapping("/{ticketId}/quantity")
    public ResponseEntity<String> updateItemQuantity(
            @PathVariable("ticketId") Long ticketId,
            @Valid @RequestBody UpdateCartItemRequest updateCartItemRequest,
            UserInfo userInfo
    ) {
        cartService.updateItemQuantity(userInfo.username(), ticketId, updateCartItemRequest);
        return ResponseEntity.ok("상품 수량이 수정되었습니다.");
    }

    // 장바구니 아이템 삭제
    @DeleteMapping("/{ticketId}")
    public ResponseEntity<String> removeCartItem(@PathVariable("ticketId") Long ticketId, UserInfo userInfo) {
        cartService.removeItemFromCart(userInfo.username(), ticketId);
        return ResponseEntity.ok("장바구니 아이템이 성공적으로 삭제되었습니다.");
    }

    // 장바구니 전체 삭제
    @DeleteMapping("/clear")
    public ResponseEntity<String> clearCart(UserInfo userInfo) {
        cartService.clearCart(userInfo.username());
        return ResponseEntity.ok("장바구니가 성공적으로 비워졌습니다.");
    }
}
