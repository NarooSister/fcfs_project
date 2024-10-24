package com.sparta.fcfsproject.unitTest;

import com.sparta.fcfsproject.common.exception.TicketBusinessException;
import com.sparta.fcfsproject.common.exception.TicketServiceErrorCode;
import com.sparta.fcfsproject.ticket.dto.TicketDto;
import com.sparta.fcfsproject.ticket.entity.Ticket;
import com.sparta.fcfsproject.ticket.repository.TicketRepository;
import com.sparta.fcfsproject.ticket.service.TicketService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class TicketServiceTest {
    @Mock
    private TicketRepository ticketRepository;

    @InjectMocks
    private TicketService ticketService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    // 모든 티켓 조회 테스트
    @Test
    @DisplayName("모든 티켓 조회 - 티켓이 없는 경우 예외 발생")
    void readAllTicket_TicketsNotFound_ThrowsException() {
        // Given
        when(ticketRepository.findAll()).thenReturn(Collections.emptyList()); // 빈 리스트를 반환

        // When & Then
        TicketBusinessException exception = assertThrows(TicketBusinessException.class, () -> {
            ticketService.readAllTicket();
        });

        // 예외 메시지 검증
        assertEquals(TicketServiceErrorCode.ALL_TICKET_NOT_FOUND, exception.getErrorCode());
    }

    @Test
    @DisplayName("모든 티켓 조회 - 여러 개의 티켓 목록을 정상적으로 반환")
    void readAllTicket_ReturnsMultipleTickets() {
        // Given
        Ticket ticket1 = new Ticket(
                "Concert Ticket 1",
                100,
                50,
                "This is the first concert ticket.",
                LocalDate.now().plusDays(30),
                Ticket.Status.ON_SALE,
                Ticket.Type.GENERAL
        );

        Ticket ticket2 = new Ticket(
                "Concert Ticket 2",
                150,
                30,
                "This is the second concert ticket.",
                LocalDate.now().plusDays(40),
                Ticket.Status.SOLD_OUT,
                Ticket.Type.SCHEDULED
        );

        when(ticketRepository.findAll()).thenReturn(List.of(ticket1, ticket2));

        // When
        List<TicketDto> result = ticketService.readAllTicket();

        // Then
        assertNotNull(result);
        assertEquals(2, result.size());

        TicketDto dto1 = result.get(0);
        assertEquals("Concert Ticket 1", dto1.getName());
        assertEquals(100, dto1.getPrice());
        assertEquals(50, dto1.getStock());
        assertEquals("This is the first concert ticket.", dto1.getDescription());
        assertEquals(Ticket.Status.ON_SALE.name(), dto1.getStatus());
        assertEquals(Ticket.Type.GENERAL.name(), dto1.getType());

        TicketDto dto2 = result.get(1);
        assertEquals("Concert Ticket 2", dto2.getName());
        assertEquals(150, dto2.getPrice());
        assertEquals(30, dto2.getStock());
        assertEquals("This is the second concert ticket.", dto2.getDescription());
        assertEquals(Ticket.Status.SOLD_OUT.name(), dto2.getStatus());
        assertEquals(Ticket.Type.SCHEDULED.name(), dto2.getType());

        verify(ticketRepository, times(1)).findAll();
    }


    // 특정 티켓 정보 조회 테스트
    @Test
    @DisplayName("특정 티켓 조회 - 티켓이 없는 경우 예외 발생")
    void readTicket_TicketNotFound_ThrowsException() {
        // Given
        Long ticketId = 1L;
        when(ticketRepository.findById(ticketId)).thenReturn(Optional.empty());

        // When & Then
        TicketBusinessException exception = assertThrows(TicketBusinessException.class, () -> {
            ticketService.readTicket(ticketId);
        });

        // 예외 메시지 검증
        assertEquals(TicketServiceErrorCode.TICKET_NOT_FOUND, exception.getErrorCode());
    }

    @Test
    @DisplayName("특정 티켓 조회 - 티켓을 정상적으로 반환")
    void readTicket_ReturnsTicket() {
        // Given
        Long ticketId = 1L;
        Ticket ticket = new Ticket(
                "Concert Ticket",
                100,
                50,
                "This is a concert ticket.",
                LocalDate.now().plusDays(30),
                Ticket.Status.ON_SALE,
                Ticket.Type.GENERAL
        );
        when(ticketRepository.findById(ticketId)).thenReturn(Optional.of(ticket));

        // When
        TicketDto result = ticketService.readTicket(ticketId);

        // Then
        assertNotNull(result);
        verify(ticketRepository, times(1)).findById(ticketId);
    }
}
