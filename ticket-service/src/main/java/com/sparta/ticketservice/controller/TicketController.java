package com.sparta.ticketservice.controller;

import com.sparta.ticketservice.dto.TicketDto;
import com.sparta.ticketservice.service.TicketService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/tickets")
public class TicketController {
    private final TicketService ticketService;
    @GetMapping("/test")
    public ResponseEntity<String> test() {
        return ResponseEntity.ok("User service is reachable through Gateway!");
    }

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
    @PostMapping("/{ticketId}/restore")
    public ResponseEntity<Void> restoreStock(@PathVariable("ticketId") Long ticketId, @RequestParam("quantity") int quantity) {
        ticketService.restoreStock(ticketId, quantity);
        return ResponseEntity.ok().build();
    }

}
