package com.sparta.orderservice.client;

import com.sparta.orderservice.dto.TicketDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;
import java.util.Map;

@FeignClient(name = "gateway") // ticket-service를 가리킴
public interface TicketClient {
    @GetMapping("/ticket-service/internal/tickets/{ticketId}")
    TicketDto getTicketById(@PathVariable("ticketId") Long ticketId);

    @PostMapping("/ticket-service/internal/tickets/{ticketId}/restore")
    void restoreStock(@PathVariable("ticketId") Long ticketId, @RequestParam("quantity") int quantity);

    @PostMapping("/ticket-service/internal/tickets/prices")
    Map<Long, Integer> getTicketPrices(@RequestParam("ticketIds") List<Long> ticketIds);

    @PostMapping("/ticket-service/internal/tickets/all")
    Map<Long, TicketDto> getTickets(@RequestParam("ticketIds") List<Long> ticketIds);

    @GetMapping("/internal/errorful/case1")
    ResponseEntity<String> callCase1();

    @GetMapping("/internal/errorful/case2")
    ResponseEntity<String> callCase2();

    @GetMapping("/internal/errorful/case3")
    ResponseEntity<String> callCase3();


}
