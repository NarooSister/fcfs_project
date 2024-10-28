package com.sparta.orderservice.dto;

import com.sparta.orderservice.entity.Orders;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@NoArgsConstructor
public class OrderDto {
    private Long id;
    private String username;
    private LocalDateTime orderDate; // 주문 생성 시간

    public OrderDto(Orders order) {
        this.id = order.getId();
        this.username = order.getUsername();
        this.orderDate = order.getCreatedAt();
    }
}
