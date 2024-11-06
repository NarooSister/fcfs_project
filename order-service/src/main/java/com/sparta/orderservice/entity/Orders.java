package com.sparta.orderservice.entity;

import com.sparta.orderservice.exception.OrderBusinessException;
import com.sparta.orderservice.exception.OrderServiceErrorCode;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;


import java.time.LocalDateTime;

@Entity
@Getter
@NoArgsConstructor
@Table(name = "orders", indexes = {
        @Index(name = "idx_user_username", columnList = "username")
})
public class Orders {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String username;
    private Integer totalPrice = 0;

    @Enumerated(EnumType.STRING)
    private OrderStatus status;

    @CreationTimestamp
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;

    public Orders(String username, Integer totalPrice, OrderStatus status) {
        this.username = username;
        this.totalPrice = totalPrice;
        this.status = status;
    }

    public Orders(String username) {
        this.username = username;
        this.status = OrderStatus.PENDING;
    }

    public void confirmOrder() {
        if (this.status != OrderStatus.PENDING) {
            throw new OrderBusinessException(OrderServiceErrorCode.CANNOT_CONFIRM_ORDER);
        }
        this.status = OrderStatus.CONFIRMED;
    }

    public void cancelOrder() {
        if (this.status != OrderStatus.CONFIRMED) {
            throw new OrderBusinessException(OrderServiceErrorCode.CANNOT_CANCEL_ORDER);
        }
        this.status = OrderStatus.CANCELED;
    }

    public void updateTotalPrice(int amount) {
        this.totalPrice += amount;
    }

    public enum OrderStatus {
        PENDING,
        CONFIRMED,
        CANCELED
    }
}
