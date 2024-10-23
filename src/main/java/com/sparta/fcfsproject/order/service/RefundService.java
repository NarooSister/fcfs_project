package com.sparta.fcfsproject.order.service;


import com.sparta.fcfsproject.order.entity.OrderedTicket;
import com.sparta.fcfsproject.ticket.entity.Ticket;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

@Service
public class RefundService {

    public double calculateRefund(Ticket ticket, OrderedTicket orderedTicket) {
        RefundPolicy refundPolicy = getRefundPolicy(ticket.getDate(), orderedTicket.getCreatedAt().toLocalDate());
        return refundPolicy.calculateRefundAmount(ticket.getPrice(), orderedTicket.getQuantity());
    }

    // 관람일과 구매일을 기준으로 환불 정책을 결정하는 메서드
    private RefundPolicy getRefundPolicy(LocalDate viewingDate, LocalDate purchaseDate) {
        long daysUntilViewing = ChronoUnit.DAYS.between(purchaseDate, viewingDate);

        if (daysUntilViewing > 9) {
            return new RefundPolicy.FullRefundPolicy();
        } else if (daysUntilViewing >= 7) {
            return new RefundPolicy.TenPercentRefundPolicy();
        } else if (daysUntilViewing >= 3) {
            return new RefundPolicy.TwentyPercentRefundPolicy();
        } else {
            return new RefundPolicy.ThirtyPercentRefundPolicy();
        }
    }
}