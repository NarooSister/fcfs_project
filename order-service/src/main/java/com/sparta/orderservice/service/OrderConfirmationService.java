package com.sparta.orderservice.service;

import com.sparta.orderservice.entity.OrderedTicket;
import com.sparta.orderservice.entity.Orders;
import com.sparta.orderservice.entity.Payment;
import com.sparta.orderservice.repository.OrderRepository;
import com.sparta.orderservice.repository.OrderedTicketRepository;
import com.sparta.orderservice.repository.PaymentRepository;
import com.sparta.orderservice.toss.PaymentResponse;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class OrderConfirmationService {
    private final OrderRepository orderRepository;
    private final OrderedTicketRepository orderedTicketRepository;
    private final PaymentRepository paymentRepository;

    public OrderConfirmationService(OrderRepository orderRepository, OrderedTicketRepository orderedTicketRepository, PaymentRepository paymentRepository) {
        this.orderRepository = orderRepository;
        this.orderedTicketRepository = orderedTicketRepository;
        this.paymentRepository = paymentRepository;
    }

    // 결제 성공 시 주문 확정
    @Transactional
    public void confirmOrderAndPayment(Orders order, PaymentResponse paymentResponse){
        // 결제 완료로 Payment 상태 설정
        Payment payment = new Payment(order.getId(), paymentResponse.getAmount(), Payment.PaymentStatus.COMPLETED);
        paymentRepository.save(payment);

        // 주문 상태를 CONFIRMED로 업데이트
        order.confirmOrder();
        orderRepository.save(order);

        // 관련된 모든 OrderedTicket을 CONFIRMED로 업데이트
        List<OrderedTicket> orderedTickets = orderedTicketRepository.findByOrderId(order.getId());
        orderedTickets.forEach(OrderedTicket::confirm); // 각 OrderedTicket을 CONFIRMED로 설정
        orderedTicketRepository.saveAll(orderedTickets);
    }
}
