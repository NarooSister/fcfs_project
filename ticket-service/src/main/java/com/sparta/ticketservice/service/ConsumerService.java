package com.sparta.ticketservice.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sparta.ticketservice.entity.Ticket;
import com.sparta.ticketservice.event.StockDecrEvent;
import com.sparta.ticketservice.event.StockIncrEvent;
import com.sparta.ticketservice.exception.TicketBusinessException;
import com.sparta.ticketservice.exception.TicketServiceErrorCode;
import com.sparta.ticketservice.repository.TicketRepository;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class ConsumerService {
    private final TicketRepository ticketRepository;

    public ConsumerService(TicketRepository ticketRepository) {
        this.ticketRepository = ticketRepository;
    }

    @KafkaListener(topics = "stock-decrement-topic", groupId = "ticket-service-group")
    @Transactional
    public void handleOrderCreatedEvent(String message) {
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            StockDecrEvent event = objectMapper.readValue(message, StockDecrEvent.class);
            log.info("Received OrderCreatedEvent for Order ID: {} and Ticket ID: {}", event.getOrderId(), event.getTicketId());

            Ticket ticket = ticketRepository.findById(event.getTicketId())
                    .orElseThrow(() -> new TicketBusinessException(TicketServiceErrorCode.TICKET_NOT_FOUND));

            // 재고 확인 및 차감
            if (ticket.getStock() < event.getQuantity()) {
                throw new TicketBusinessException(TicketServiceErrorCode.INSUFFICIENT_STOCK);
            }

            ticket.decrementStock(event.getQuantity());
            ticketRepository.save(ticket);

            log.info("Stock updated for Ticket ID: {}. New stock: {}", ticket.getId(), ticket.getStock());
            log.info("Received event: {}", event);
        } catch (Exception e) {
            log.error("Failed to deserialize message: {}", message, e);
        }


    }

    // 재고 복구 이벤트 처리
    @KafkaListener(topics = "stock-restore-topic", groupId = "ticket-service-group")
    @Transactional
    public void handleStockRestoreEvent(String message) {
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            StockDecrEvent event = objectMapper.readValue(message, StockDecrEvent.class);
            log.info("Received handleStockRestoreEvent for Order ID: {} and Ticket ID: {}", event.getOrderId(), event.getTicketId());
            Ticket ticket = ticketRepository.findById(event.getTicketId())
                    .orElseThrow(() -> new TicketBusinessException(TicketServiceErrorCode.TICKET_NOT_FOUND));

            // 재고 복구
            ticket.incrementStock(event.getQuantity());
            ticketRepository.save(ticket);

            log.info("Stock restored for Ticket ID: {}. New stock: {}", ticket.getId(), ticket.getStock());

        } catch (Exception e) {
            log.error("Failed to deserialize message: {}", message, e);
        }
    }
}
