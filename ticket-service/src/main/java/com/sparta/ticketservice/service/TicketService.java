package com.sparta.ticketservice.service;

import com.sparta.ticketservice.dto.TicketDto;
import com.sparta.ticketservice.entity.Ticket;
import com.sparta.ticketservice.exception.TicketBusinessException;
import com.sparta.ticketservice.exception.TicketServiceErrorCode;
import com.sparta.ticketservice.repository.TicketRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
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

    public void restoreStock(Long ticketId, int quantity) {
        // 티켓 조회
        Ticket ticket = ticketRepository.findById(ticketId)
                .orElseThrow(() -> new TicketBusinessException(TicketServiceErrorCode.TICKET_NOT_FOUND));

        // 재고 복구 로직
        ticket.incrementStock(quantity);
        ticketRepository.save(ticket);
    }

    public Map<Long, Integer> getTicketPrices(List<Long> ticketIds) {
        // ticketIds에 포함된 ID로 각각의 Ticket 가격을 조회하여 Map으로 반환
        return ticketRepository.findAllById(ticketIds).stream()
                .collect(Collectors.toMap(
                        Ticket::getId,
                        Ticket::getPrice
                ));
    }

    public Map<Long, TicketDto> getTicketAllById(List<Long> ticketIds) {
        return ticketRepository.findAllById(ticketIds).stream()
                .collect(Collectors.toMap(
                        Ticket::getId,
                        ticket -> new TicketDto(ticket.getId(), ticket.getPrice(), ticket.getDate()) // Map의 값으로 TicketDto 생성
                ));
    }
}
