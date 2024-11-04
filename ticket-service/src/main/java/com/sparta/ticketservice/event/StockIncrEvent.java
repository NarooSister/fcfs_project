package com.sparta.ticketservice.event;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@AllArgsConstructor
@NoArgsConstructor
public class StockIncrEvent {
    private Long ticketId;
    private Integer quantity;
}
