package com.sparta.fcfsproject.order.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class OrderedTicketDto {
    private Long ticketId;   // 티켓 ID
    private Integer quantity; // 구매 수량
}
