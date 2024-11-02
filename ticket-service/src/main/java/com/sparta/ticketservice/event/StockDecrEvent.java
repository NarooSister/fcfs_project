package com.sparta.ticketservice.event;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class StockDecrEvent {
    private Long orderId;
    private Long ticketId;
    private Integer quantity;
}
