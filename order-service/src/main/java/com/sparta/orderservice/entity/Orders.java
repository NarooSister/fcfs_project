package com.sparta.orderservice.entity;

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
@AllArgsConstructor
@Table(name = "orders", indexes = {
        @Index(name = "idx_user_username", columnList = "username")
})
public class Orders{
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String username;
    private Integer totalPrice = 0;

    @CreationTimestamp
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;

    public Orders(String username) {
        this.username = username;
    }

    public void updateTotalPrice(int amount) {
        this.totalPrice += amount;
    }
}
