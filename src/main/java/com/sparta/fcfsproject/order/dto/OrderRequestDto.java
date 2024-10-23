package com.sparta.fcfsproject.order.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;
@Getter
@NoArgsConstructor
public class OrderRequestDto {
    private List<OrderedTicketDto> orderedTickets;  // 주문된 티켓 리스트
}
