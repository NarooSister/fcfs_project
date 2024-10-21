package com.sparta.fcfsproject.ticket.dto;

import com.sparta.fcfsproject.ticket.entity.Ticket;
import lombok.Getter;

@Getter
public class TicketDto {
    private Long id;
    private String name;
    private Integer price;
    private Integer stock;
    private String description;
    private String status;
    private String type;
    public TicketDto(Ticket ticket) {
        this.id = ticket.getId();
        this.name = ticket.getName();
        this.price = ticket.getPrice();
        this.stock = ticket.getStock();
        this.description = ticket.getDescription();
        this.status = ticket.getStatus().name();
        this.type = ticket.getType().name();
    }

}
