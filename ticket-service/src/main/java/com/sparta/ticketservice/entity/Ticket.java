package com.sparta.ticketservice.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class Ticket{
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;

    private Integer price;

    private Integer stock;

    private String description;

    private LocalDate date; // 티켓 예약 날짜

    @CreationTimestamp
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;

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
    public void decrementStock(int quantity) {
        this.stock -= quantity;
    }

    // 재고 복구
    public void incrementStock(int quantity) {
        this.stock += quantity;
    }

    public Ticket(String name, Integer price, Integer stock, String description, LocalDate date, Status status, Type type) {
        this.name = name;
        this.price = price;
        this.stock = stock;
        this.description = description;
        this.date = date;
        this.status = status;
        this.type = type;
    }
}
