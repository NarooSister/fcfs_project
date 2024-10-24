package com.sparta.fcfsproject.order.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;
@Getter
@Setter
@NoArgsConstructor
public class OrderRequestDto {
    private List<OrderedTicketDto> orderedTickets;  // 주문된 티켓 리스트
}
