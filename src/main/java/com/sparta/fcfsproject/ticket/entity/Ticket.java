package com.sparta.fcfsproject.ticket.entity;

import com.sparta.fcfsproject.common.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

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
}
