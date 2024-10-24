package com.sparta.fcfsproject.ticket.controller;

import com.sparta.fcfsproject.ticket.dto.TicketDto;
import com.sparta.fcfsproject.ticket.service.TicketService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/tickets")
public class TicketController {
    private final TicketService ticketService;
    @GetMapping
    public ResponseEntity<List<TicketDto>> readAllTicket() {
        // 서비스에서 모든 티켓을 조회하여 반환
        List<TicketDto> tickets = ticketService.readAllTicket();
        return ResponseEntity.ok(tickets);
    }

    @GetMapping("/{ticketId}")
    public ResponseEntity<TicketDto> readTicket(
            @PathVariable("ticketId") Long ticketId
    ){
        TicketDto ticket = ticketService.readTicket(ticketId);
        return ResponseEntity.ok(ticket);
    }
}
