package com.sparta.orderservice.event;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class StockDecrEvent {
    private Long orderId;
    private String username;
    private Integer totalPrice;
}
