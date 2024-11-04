package com.sparta.orderservice.event;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@AllArgsConstructor
@NoArgsConstructor
public class StockDecrEvent {
    private Long orderId;
    private String username;
    private Integer totalPrice;
}
