package com.sparta.ticketservice.event;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class StockIncrEvent {
    private Long ticketId;
    private Integer quantity;
}
