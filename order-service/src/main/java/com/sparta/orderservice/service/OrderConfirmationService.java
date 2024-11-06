package com.sparta.orderservice.service;

import com.sparta.orderservice.dto.PendingOrder;
import com.sparta.orderservice.entity.OrderedTicket;
import com.sparta.orderservice.entity.Orders;
import com.sparta.orderservice.entity.Payment;
import com.sparta.orderservice.repository.OrderRepository;
import com.sparta.orderservice.repository.OrderedTicketRepository;
import com.sparta.orderservice.repository.PaymentRepository;
import com.sparta.orderservice.repository.PendingOrderRepository;
import com.sparta.orderservice.toss.PaymentResponse;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class OrderConfirmationService {
    private final OrderRepository orderRepository;
    private final OrderedTicketRepository orderedTicketRepository;
    private final PendingOrderRepository pendingOrderRepository;
    private final PaymentRepository paymentRepository;

    // 결제 성공 시 주문 확정
    @Transactional
    public void confirmOrderAndPayment(PaymentResponse paymentResponse, List<PendingOrder> pendingOrders){
        // Order 객체 생성 및 저장
        Orders order = new Orders(pendingOrders.get(0).getUsername(), paymentResponse.getAmount(), Orders.OrderStatus.CONFIRMED);
        orderRepository.save(order);

        // 결제 완료로 Payment 상태 설정
        Payment payment = new Payment(order.getId(), paymentResponse.getAmount(), Payment.PaymentStatus.COMPLETED);
        paymentRepository.save(payment);

        // 각 PendingOrder를 기반으로 OrderTicket 생성 및 저장
        List<OrderedTicket> orderedTickets = pendingOrders.stream()
                .map(pendingOrder -> {
                    OrderedTicket orderedTicket = OrderedTicket.createPending(
                            order.getId(),
                            pendingOrder.getTicketId(),
                            pendingOrder.getQuantity(),
                            paymentResponse.getAmount() / pendingOrders.size()
                    );
                    orderedTicket.confirm();  // 상태를 CONFIRMED로 설정
                    return orderedTicket;
                })
                .toList();

        orderedTicketRepository.saveAll(orderedTickets);
    }
}
