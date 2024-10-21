package com.sparta.fcfsproject.ticket.repository;

import com.sparta.fcfsproject.ticket.entity.Ticket;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TicketRepository extends JpaRepository<Ticket, Long> {
}
