package com.sparta.orderservice.unit_test;

import com.sparta.orderservice.dto.OrderedTicketDto;
import com.sparta.orderservice.dto.TicketDto;
import com.sparta.orderservice.entity.OrderedTicket;
import com.sparta.orderservice.service.RefundService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;

class RefundServiceTest {
    private RefundService refundService;

    @BeforeEach
    void setUp() {
        refundService = new RefundService();
    }

    @Test
    @DisplayName("9일 전 구매 시 전액 환불")
    void testFullRefundPolicy() {
        Long orderId = 1L; // orderId 정의
        Long ticketId = 1L; // ticketId 정의

        TicketDto ticket = new TicketDto(ticketId, "ON_SALE", 100, 100, LocalDate.now().plusDays(10));

        OrderedTicket orderedTicket = new OrderedTicket(1L, ticketId, orderId, 1, 100,LocalDateTime.now(), LocalDateTime.now(), OrderedTicket.Status.PENDING);

        double refundAmount = refundService.calculateRefund(ticket, orderedTicket);

        assertEquals(100.0, refundAmount); // 전액 환불 확인
    }

    @Test
    @DisplayName("7일 전 구매 시 10% 수수료 환불")
    void testTenPercentRefundPolicy() {
        Long orderId = 1L; // orderId 정의
        Long ticketId = 1L; // ticketId 정의

        TicketDto ticket = new TicketDto(ticketId, "ON_SALE", 100, 100, LocalDate.now().plusDays(7));

        OrderedTicket orderedTicket = new OrderedTicket(1L, ticketId, orderId, 1, 100,LocalDateTime.now(), LocalDateTime.now(), OrderedTicket.Status.PENDING);

        double refundAmount = refundService.calculateRefund(ticket, orderedTicket);

        assertEquals(90.0, refundAmount); // 10% 수수료
    }

    @Test
    @DisplayName("3일 전 구매 시 20% 수수료 환불")
    void testTwentyPercentRefundPolicy() {
        Long orderId = 1L; // orderId 정의
        Long ticketId = 1L; // ticketId 정의
        TicketDto ticket = new TicketDto(ticketId, "ON_SALE", 100, 100, LocalDate.now().plusDays(3));

        OrderedTicket orderedTicket = new OrderedTicket(1L, ticketId, orderId, 1, 100,LocalDateTime.now(), LocalDateTime.now(), OrderedTicket.Status.PENDING);

        double refundAmount = refundService.calculateRefund(ticket, orderedTicket);

        assertEquals(80.0, refundAmount); // 20% 수수료
    }

    @Test
    @DisplayName("관람일 당일 구매 시 30% 수수료 환불")
    void testThirtyPercentRefundPolicy() {
        Long orderId = 1L; // orderId 정의
        Long ticketId = 1L; // ticketId 정의

        TicketDto ticket = new TicketDto(ticketId, "ON_SALE", 100, 100, LocalDate.now().plusDays(0));

        OrderedTicket orderedTicket = new OrderedTicket(1L, ticketId, orderId, 1, 100,LocalDateTime.now(), LocalDateTime.now(), OrderedTicket.Status.PENDING);

        double refundAmount = refundService.calculateRefund(ticket, orderedTicket);

        assertEquals(70.0, refundAmount); // 30% 수수료
    }
}
