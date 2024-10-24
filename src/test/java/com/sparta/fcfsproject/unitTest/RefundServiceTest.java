package com.sparta.fcfsproject.unitTest;

import com.sparta.fcfsproject.order.service.RefundService;
import com.sparta.fcfsproject.ticket.entity.Ticket;
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
        Ticket ticket = new Ticket(ticketId, "Concert Ticket", 100, 10, "This is a concert ticket.",
                LocalDate.now().plusDays(10), Ticket.Status.ON_SALE, Ticket.Type.GENERAL);

        // MockOrderedTicket 객체를 생성
        MockOrderedTicket orderedTicket = new MockOrderedTicket(1L, orderId, ticketId, 1, 100);
        orderedTicket.setCreatedAt(LocalDateTime.now().minusDays(9)); // 구매일을 9일 전으로 설정

        double refundAmount = refundService.calculateRefund(ticket, orderedTicket);

        assertEquals(100.0, refundAmount); // 전액 환불 확인
    }

    @Test
    @DisplayName("7일 전 구매 시 10% 수수료 환불")
    void testTenPercentRefundPolicy() {
        Long orderId = 1L; // orderId 정의
        Long ticketId = 1L; // ticketId 정의
        Ticket ticket = new Ticket(ticketId, "Concert Ticket", 100, 10, "This is a concert ticket.",
                LocalDate.now().plusDays(7), Ticket.Status.ON_SALE, Ticket.Type.GENERAL);

        // MockOrderedTicket 객체를 생성
        MockOrderedTicket orderedTicket = new MockOrderedTicket(1L, orderId, ticketId, 1, 100);
        orderedTicket.setCreatedAt(LocalDateTime.now()); // 7일 전 구매

        double refundAmount = refundService.calculateRefund(ticket, orderedTicket);

        assertEquals(90.0, refundAmount); // 10% 수수료
    }

    @Test
    @DisplayName("3일 전 구매 시 20% 수수료 환불")
    void testTwentyPercentRefundPolicy() {
        Long orderId = 1L; // orderId 정의
        Long ticketId = 1L; // ticketId 정의
        Ticket ticket = new Ticket(ticketId, "Concert Ticket", 100, 10, "This is a concert ticket.",
                LocalDate.now().plusDays(3), Ticket.Status.ON_SALE, Ticket.Type.GENERAL);

        // MockOrderedTicket 객체를 생성
        MockOrderedTicket orderedTicket = new MockOrderedTicket(1L, orderId, ticketId, 1, 100);
        orderedTicket.setCreatedAt(LocalDateTime.now());

        double refundAmount = refundService.calculateRefund(ticket, orderedTicket);

        assertEquals(80.0, refundAmount); // 20% 수수료
    }

    @Test
    @DisplayName("관람일 당일 구매 시 30% 수수료 환불")
    void testThirtyPercentRefundPolicy() {
        Long orderId = 1L; // orderId 정의
        Long ticketId = 1L; // ticketId 정의
        Ticket ticket = new Ticket(ticketId, "Concert Ticket", 100, 10, "This is a concert ticket.",
                LocalDate.now().plusDays(0), Ticket.Status.ON_SALE, Ticket.Type.GENERAL);

        // MockOrderedTicket 객체를 생성
        MockOrderedTicket orderedTicket = new MockOrderedTicket(1L, orderId, ticketId, 1, 100);
        orderedTicket.setCreatedAt(LocalDateTime.now()); // 오늘 구매

        double refundAmount = refundService.calculateRefund(ticket, orderedTicket);

        assertEquals(70.0, refundAmount); // 30% 수수료
    }
}