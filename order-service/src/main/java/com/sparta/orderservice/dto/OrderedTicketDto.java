package com.sparta.orderservice.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class OrderedTicketDto {
    private Long ticketId;   // 티켓 ID
    private Integer price;
    private Integer quantity; // 구매 수량

    public OrderedTicketDto(Long ticketId, Integer price, Integer quantity) {
        this.ticketId = ticketId;
        this.price = price;
        this.quantity = quantity;
    }
}
