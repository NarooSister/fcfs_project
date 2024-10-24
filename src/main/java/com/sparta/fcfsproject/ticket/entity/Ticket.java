package com.sparta.fcfsproject.ticket.entity;

import com.sparta.fcfsproject.common.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Entity
@Getter
@NoArgsConstructor
public class Ticket extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;

    private Integer price;

    private Integer stock;

    private String description;

    private LocalDate date; // 티켓 예약 날짜

    @Enumerated(EnumType.STRING)
    private Status status;

    @Enumerated(EnumType.STRING)
    private Type type;

    public enum Status{
        PENDING,    // 준비 중
        ON_SALE,    // 판매 중
        SOLD_OUT,   // 판매 완료(재고 소진)
        EXPIRED     // 판매 만료(기한 만료)
    }
    public enum Type{
        GENERAL,    // 일반 티켓
        SCHEDULED   // 시간대별 티켓
    }

    // 재고 차감 메서드
    public void reduceStock(int quantity) {
        this.stock -= quantity;
    }

    // 추가 메서드 예시 (재고 복구)
    public void restoreStock(int quantity) {
        this.stock += quantity;
    }
}
