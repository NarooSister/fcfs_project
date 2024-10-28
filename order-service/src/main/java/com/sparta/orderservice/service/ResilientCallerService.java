package com.sparta.orderservice.service;

import com.sparta.orderservice.client.TicketClient;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import org.springframework.stereotype.Service;

@Service
public class ResilientCallerService {
    private final TicketClient ticketClient;

    public ResilientCallerService(TicketClient ticketClient) {
        this.ticketClient = ticketClient;
    }

    @CircuitBreaker(name = "ticket-service", fallbackMethod = "fallback")
    @Retry(name = "ticket-service")
    public String callCase1WithResilience() {
        return ticketClient.callCase3().getBody();
    }

    public String fallback(Exception e) {
        return "Fallback response: " + e.getMessage();
    }
}



