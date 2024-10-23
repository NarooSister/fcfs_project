package com.sparta.fcfsproject.order.entity;

import com.sparta.fcfsproject.common.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor
@Table(name = "cart", indexes = {
        @Index(name = "idx_user_id", columnList = "user_id"),
        @Index(name = "idx_ticket_id", columnList = "ticket_id")
})
public class Cart extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long userId;

    private Long ticketId;

    private Integer quantity;
    public static Cart create(Long userId, Long ticketId, Integer quantity) {
        Cart cart = new Cart();
        cart.userId = userId;
        cart.ticketId = ticketId;
        cart.quantity = quantity;
        return cart;
    }

    // 수량 업데이트
    public void updateQuantity(Integer quantity) {
        this.quantity = quantity;
    }
}
