package com.sparta.fcfsproject.order.repository;

import com.sparta.fcfsproject.order.entity.OrderedTicket;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OrderedTicketRepository extends JpaRepository<OrderedTicket, Long> {
}
