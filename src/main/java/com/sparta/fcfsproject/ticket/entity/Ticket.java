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
    private Type type;
    public enum Type{
        GENERAL,    // 일반 티켓
        SCHEDULED   // 시간대별 티켓
    }
}
