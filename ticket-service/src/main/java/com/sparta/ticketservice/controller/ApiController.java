package com.sparta.ticketservice.controller;

import com.sparta.ticketservice.dto.TicketDto;
import com.sparta.ticketservice.service.TicketService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/internal/tickets")
public class ApiController {
    private final TicketService ticketService;

    public ApiController(TicketService ticketService) {
        this.ticketService = ticketService;
    }

    @PostMapping("/{ticketId}/restore")
    public ResponseEntity<Void> restoreStock(@PathVariable("ticketId") Long ticketId, @RequestParam("quantity") int quantity) {
        ticketService.restoreStock(ticketId, quantity);
        return ResponseEntity.ok().build();
    }
    @GetMapping("/{ticketId}")
    public ResponseEntity<TicketDto> readTicket(
            @PathVariable("ticketId") Long ticketId
    ){
        TicketDto ticket = ticketService.readTicket(ticketId);
        return ResponseEntity.ok(ticket);
    }

    @PostMapping("/prices")
    public Map<Long, Integer> getTicketPrices(@RequestParam("ticketIds") List<Long> ticketIds) {
        return ticketService.getTicketPrices(ticketIds);
    }
}
