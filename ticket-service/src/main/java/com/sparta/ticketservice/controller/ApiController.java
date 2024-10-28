package com.sparta.ticketservice.controller;

import com.sparta.ticketservice.dto.TicketDto;
import com.sparta.ticketservice.service.TicketService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/internal")
public class ApiController {
    private final TicketService ticketService;

    public ApiController(TicketService ticketService) {
        this.ticketService = ticketService;
    }

    @PostMapping("/tickets/{ticketId}/restore")
    public ResponseEntity<Void> restoreStock(@PathVariable("ticketId") Long ticketId, @RequestParam("quantity") int quantity) {
        ticketService.restoreStock(ticketId, quantity);
        return ResponseEntity.ok().build();
    }
    @GetMapping("/tickets/{ticketId}")
    public ResponseEntity<TicketDto> readTicket(
            @PathVariable("ticketId") Long ticketId
    ){
        TicketDto ticket = ticketService.readTicket(ticketId);
        return ResponseEntity.ok(ticket);
    }
}
