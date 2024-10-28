package com.sparta.orderservice.client;

import com.sparta.orderservice.config.FeignConfig;
import com.sparta.orderservice.dto.TicketDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient(name = "ticket-service", configuration = FeignConfig.class) // ticket-service를 가리킴
public interface TicketClient {
    @GetMapping("/tickets/{ticketId}")
    TicketDto getTicketById(@PathVariable("ticketId") Long ticketId);

    @PostMapping("/tickets/{ticketId}/restore")
    void restoreStock(@PathVariable("ticketId") Long ticketId, @RequestParam("quantity") int quantity);
}
