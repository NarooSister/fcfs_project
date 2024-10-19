package com.sparta.fcfsproject.order.entity;

import com.sparta.fcfsproject.common.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor
@Table(name = "Order", indexes = {
        @Index(name = "idx_user_id", columnList = "user_id")
})
public class Order extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private Long userId;
    @Enumerated(EnumType.STRING)
    private Status status;
    public enum Status {
        PENDING,   // 주문 처리 중
        SHIPPED,   // 배송 중
        DELIVERED, // 배송 완료
        CANCELED,   // 취소 완료
        RETURNED    // 반품 완료
    }
}
