package com.sparta.orderservice.dto;

import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PendingOrder {
    private String username;
    private Long ticketId;
    private Integer price;
    private Integer quantity;
    private PendingStatus status;
    private LocalDateTime createdAt;

    public enum PendingStatus {
        PENDING,
        COMPLETED,
        CANCELLED
    }
}
