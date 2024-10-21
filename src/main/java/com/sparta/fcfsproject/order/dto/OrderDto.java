package com.sparta.fcfsproject.order.dto;

import com.sparta.fcfsproject.order.entity.Orders;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
public class OrderDto {
    private Long id;
    private LocalDateTime orderDate; // 주문 생성 시간
    private String status; // 주문 상태

    public OrderDto(Orders order) {
        this.id = order.getId();
        this.orderDate = order.getCreatedAt();
        this.status = order.getStatus().name();
    }
}
