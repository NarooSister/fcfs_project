package com.sparta.fcfsproject.order.dto;

import com.sparta.fcfsproject.order.entity.Orders;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@NoArgsConstructor
public class OrderDto {
    private Long id;
    private Long userId;
    private LocalDateTime orderDate; // 주문 생성 시간

    public OrderDto(Orders order) {
        this.id = order.getId();
        this.userId = order.getUserId();
        this.orderDate = order.getCreatedAt();
    }
}
