package com.sparta.fcfsproject.order.entity;

import com.sparta.fcfsproject.common.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor
@Table(name = "orders", indexes = {
        @Index(name = "idx_user_id", columnList = "user_id")
})
public class Orders extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private Long userId;
    private Integer totalPrice = 0;

    public Orders(Long userId) {
        this.userId = userId;
    }

    public void updateTotalPrice(int amount) {
        this.totalPrice += amount;
    }
}
