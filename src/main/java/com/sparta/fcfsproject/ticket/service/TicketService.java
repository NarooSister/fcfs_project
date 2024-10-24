package com.sparta.fcfsproject.ticket.service;

import com.sparta.fcfsproject.common.exception.TicketBusinessException;
import com.sparta.fcfsproject.common.exception.TicketServiceErrorCode;
import com.sparta.fcfsproject.ticket.dto.TicketDto;
import com.sparta.fcfsproject.ticket.entity.Ticket;
import com.sparta.fcfsproject.ticket.repository.TicketRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Slf4j
public class TicketService {
    private final TicketRepository ticketRepository;

    public TicketService(TicketRepository ticketRepository) {
        this.ticketRepository = ticketRepository;
    }

    // 모든 티켓 조회
    public List<TicketDto> readAllTicket() {
        List<Ticket> tickets = ticketRepository.findAll();

        if (tickets.isEmpty()) {
            throw new TicketBusinessException(TicketServiceErrorCode.ALL_TICKET_NOT_FOUND);
        }
        return tickets.stream()
                .map(TicketDto::new)
                .collect(Collectors.toList());
    }

    // 특정 티켓 정보 조회
    public TicketDto readTicket(Long ticketId) {
        Ticket ticket = ticketRepository.findById(ticketId)
                .orElseThrow(() -> new TicketBusinessException(TicketServiceErrorCode.TICKET_NOT_FOUND));

        return new TicketDto(ticket);
    }
}
