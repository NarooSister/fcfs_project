package com.sparta.fcfsproject.order.repository;

import com.sparta.fcfsproject.order.entity.OrderedTicket;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
@Repository
public interface OrderedTicketRepository extends JpaRepository<OrderedTicket, Long> {
    List<OrderedTicket> findByOrderId(Long orderId);
}
