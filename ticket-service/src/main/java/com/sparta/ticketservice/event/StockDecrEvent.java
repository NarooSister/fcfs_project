package com.sparta.ticketservice.event;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@AllArgsConstructor
@NoArgsConstructor
public class StockDecrEvent {
    private Long orderId;
    private Long ticketId;
    private Integer quantity;
}
