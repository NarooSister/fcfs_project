package com.sparta.orderservice.entity;

import jakarta.persistence.*;
import lombok.Getter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Getter
public class Payment {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private Long orderId;
    private Integer amount;
    @Enumerated(EnumType.STRING)
    private PaymentStatus status;
    @Enumerated(EnumType.STRING)
    private PaymentType paymentType;
    @CreationTimestamp
    private LocalDateTime createdAt;
    @UpdateTimestamp
    private LocalDateTime updatedAt;

    public Payment(Long orderId, int amount, PaymentStatus paymentStatus) {
        this.orderId = orderId;
        this.amount = amount;
        this.status = paymentStatus;
    }

    public Payment() {

    }

    public enum PaymentStatus {
        PENDING,        // 결제 대기
        PROCESSING,     // 결제 중
        COMPLETED,      // 결제 완료
        FAILED,         // 결제 실패
        REFUNDED        // 환불 완료
    }

    public enum PaymentType {
        CREDIT_CARD, // 카드 결제
        BANK_TRANSFER, // 계좌이체
        PAYPAL,     // 페이팔 등 기타 결제 방식 추가 가능
        MOBILE      // 모바일 결제
    }
}
