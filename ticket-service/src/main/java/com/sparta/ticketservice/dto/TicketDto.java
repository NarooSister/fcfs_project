package com.sparta.ticketservice.dto;

import com.sparta.ticketservice.entity.Ticket;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Getter
@NoArgsConstructor
public class TicketDto {
    private Long id;
    private String name;
    private Integer price;
    private Integer stock;
    private String description;
    private LocalDate date;
    private String status;
    private String type;

    public TicketDto(Ticket ticket) {
        this.id = ticket.getId();
        this.name = ticket.getName();
        this.price = ticket.getPrice();
        this.stock = ticket.getStock();
        this.description = ticket.getDescription();
        this.date = ticket.getDate();
        this.status = ticket.getStatus().name();
        this.type = ticket.getType().name();
    }

    public TicketDto(Long id, Integer price, LocalDate date) {
        this.id = id;
        this.price = price;
        this.date = date;
    }
}
