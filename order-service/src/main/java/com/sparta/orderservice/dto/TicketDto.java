package com.sparta.orderservice.dto;

import java.time.LocalDate;

public record TicketDto(Long id, String status, int stock, int price, LocalDate date) {
}
