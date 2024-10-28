package com.sparta.orderservice.service;

import com.sparta.orderservice.dto.TicketDto;
import com.sparta.orderservice.entity.OrderedTicket;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

@Service
public class RefundService {
    private static final Logger logger = LoggerFactory.getLogger(RefundService.class);

    public double calculateRefund(TicketDto ticket, OrderedTicket orderedTicket) {
        RefundPolicy refundPolicy = getRefundPolicy(ticket.date(), orderedTicket.getCreatedAt().toLocalDate());
        return refundPolicy.calculateRefundAmount(ticket.price(), orderedTicket.getQuantity());
    }

    // 관람일과 구매일을 기준으로 환불 정책을 결정하는 메서드
    private RefundPolicy getRefundPolicy(LocalDate viewingDate, LocalDate purchaseDate) {
        if (viewingDate == null || purchaseDate == null) {
            System.out.println("viewingDate = " + viewingDate);
            System.out.println("purchaseDate = " + purchaseDate);
            throw new IllegalArgumentException("관람일 또는 구매일이 유효하지 않습니다.");
        }
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