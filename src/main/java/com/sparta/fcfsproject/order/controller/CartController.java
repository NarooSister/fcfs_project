package com.sparta.fcfsproject.order.controller;

import com.sparta.fcfsproject.auth.config.AuthFacade;
import com.sparta.fcfsproject.auth.entity.User;
import com.sparta.fcfsproject.order.entity.CartItem;
import com.sparta.fcfsproject.order.service.CartService;
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
    public ResponseEntity<String> addToCart(@RequestBody CartItem cartItem) {
        User user = AuthFacade.getCurrentUser();
        cartService.addItemToCart(user, cartItem);
        return ResponseEntity.ok("장바구니에 아이템이 성공적으로 추가되었습니다.");
    }

    // 장바구니 조회
    @GetMapping
    public ResponseEntity<Map<String, CartItem>> getCartItems() {
        User user = AuthFacade.getCurrentUser();
        Map<String, CartItem> cartItems = cartService.getCart(user);
        return ResponseEntity.ok(cartItems);
    }

    // 장바구니 수량 수정
    @PatchMapping("/{ticketId}")
    public ResponseEntity<String> updateItemQuantity(@PathVariable("ticketId") Long ticketId, @RequestParam Integer newQuantity){
        User user = AuthFacade.getCurrentUser();
        cartService.updateItemQuantity(user, ticketId, newQuantity);
        return ResponseEntity.ok("상품 수량이 수정되었습니다.");
    }

    // 장바구니 아이템 삭제
    @DeleteMapping("/{ticketId}")
    public ResponseEntity<String> removeCartItem(@PathVariable("ticketId") Long ticketId) {
        User user = AuthFacade.getCurrentUser();
        cartService.removeItemFromCart(user, ticketId);
        return ResponseEntity.ok("장바구니 아이템이 성공적으로 삭제되었습니다.");
    }

    // 장바구니 전체 삭제
    @DeleteMapping("/clear")
    public ResponseEntity<String> clearCart() {
        User user = AuthFacade.getCurrentUser();
        cartService.clearCart(user);
        return ResponseEntity.ok("장바구니가 성공적으로 비워졌습니다.");
    }

}
