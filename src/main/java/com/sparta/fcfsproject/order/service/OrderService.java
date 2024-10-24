package com.sparta.fcfsproject.order.service;

import com.sparta.fcfsproject.auth.entity.User;
import com.sparta.fcfsproject.common.exception.OrderBusinessException;
import com.sparta.fcfsproject.common.exception.OrderServiceErrorCode;
import com.sparta.fcfsproject.order.dto.OrderDto;
import com.sparta.fcfsproject.order.dto.OrderRequestDto;
import com.sparta.fcfsproject.order.dto.OrderedTicketDto;
import com.sparta.fcfsproject.order.entity.OrderedTicket;
import com.sparta.fcfsproject.order.entity.Orders;
import com.sparta.fcfsproject.order.repository.OrderRepository;
import com.sparta.fcfsproject.order.repository.OrderedTicketRepository;
import com.sparta.fcfsproject.ticket.entity.Ticket;
import com.sparta.fcfsproject.ticket.repository.TicketRepository;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class OrderService {
    private final OrderRepository orderRepository;
    private final OrderedTicketRepository orderedTicketRepository;
    private final TicketRepository ticketRepository;
    private final RefundService refundService;

    public OrderService(OrderRepository orderRepository, OrderedTicketRepository orderedTicketRepository, TicketRepository ticketRepository, RefundService refundService) {
        this.orderRepository = orderRepository;
        this.ticketRepository = ticketRepository;
        this.orderedTicketRepository = orderedTicketRepository;
        this.refundService = refundService;
    }

    // 사용자의 모든 주문 가져오기
    public List<OrderDto> readAllOrders(User user) {
        List<Orders> orderList = orderRepository.findAllByUserId(user.getId());
        if (orderList.isEmpty()) {
            throw new OrderBusinessException(OrderServiceErrorCode.ALL_ORDER_NOT_FOUND);
        }
        return orderList.stream()
                .map(OrderDto::new)
                .collect(Collectors.toList());
    }

    // 사용자의 주문 상세 내역 가져오기
    public OrderDto readOrder(User user, Long orderId) {
        Orders order = orderRepository.findByIdAndUserId(orderId, user.getId())
                .orElseThrow(() -> new OrderBusinessException(OrderServiceErrorCode.ORDER_NOT_FOUND)
                );
        return new OrderDto(order);
    }

    // 기본 주문 생성 구현 (TODO : 주문 생성 나중에 추가)
    @Transactional
    public void createOrder(User user, OrderRequestDto orderRequestDto) {
        // 주문 티켓 리스트 가져오기
        List<OrderedTicketDto> orderedTickets = orderRequestDto.getOrderedTickets();
        // 주문 생성
        Orders order = new Orders(user.getId());

        // 주문 처리
        for (OrderedTicketDto orderedTicketDto : orderedTickets) {
            // 티켓 가져오기
            Ticket ticket = ticketRepository.findById(orderedTicketDto.getTicketId())
                    .orElseThrow(() -> new OrderBusinessException(OrderServiceErrorCode.TICKET_NOT_FOUND));

            // 판매 중이 아닌 경우
            if (!Ticket.Status.ON_SALE.equals(ticket.getStatus())) {
                throw new OrderBusinessException(OrderServiceErrorCode.TICKET_NOT_ON_SALE);
            }

            // 재고 확인
            if (ticket.getStock() < orderedTicketDto.getQuantity()) {
                throw new OrderBusinessException(OrderServiceErrorCode.INSUFFICIENT_STOCK);
            }

            // 재고 차감
            ticket.reduceStock(orderedTicketDto.getQuantity());
            ticketRepository.save(ticket);  // 재고 업데이트

            // 오더 티켓 생성
            OrderedTicket orderedTicket = OrderedTicket.createPending(
                    order.getId(),
                    ticket.getId(),
                    orderedTicketDto.getQuantity(),
                    ticket.getPrice() * orderedTicketDto.getQuantity()
            );
            orderedTicketRepository.save(orderedTicket);

            order.updateTotalPrice(orderedTicket.getPrice());
        }
        orderRepository.save(order);
        // TODO : 결제 프로세스로 연결
    }

    // 주문 취소 로직
    public void cancelOrder(User user, Long orderId) {
        // 사용자와 주문 아이디로 주문 찾기
        Orders order = orderRepository.findByIdAndUserId(orderId, user.getId())
                .orElseThrow(() -> new OrderBusinessException(OrderServiceErrorCode.ORDER_NOT_FOUND));

        List<OrderedTicket> orderedTickets = orderedTicketRepository.findByOrderId(orderId);

        for (OrderedTicket orderedTicket : orderedTickets) {
            // 티켓 가져오기
            Ticket ticket = ticketRepository.findById(orderedTicket.getTicketId())
                    .orElseThrow(() -> new OrderBusinessException(OrderServiceErrorCode.TICKET_NOT_FOUND));

            // 티켓 관람일 기준으로 환불 금액 계산
            double refundAmount = refundService.calculateRefund(ticket, orderedTicket);

            // 환불 처리 (결제 시스템)
            processRefund(refundAmount);

            // 티켓 취소 처리
            orderedTicket.cancel();
            orderedTicketRepository.save(orderedTicket);

            // 재고 복구
            ticket.restoreStock(orderedTicket.getQuantity());
            ticketRepository.save(ticket);
        }
        orderRepository.save(order);
    }

    private void processRefund(double refundAmount) {
        // TODO : 환불 로직
    }

    public void reduceStock(Orders order) {
        // TODO : 재고 감소 로직
    }

    public void restoreStock(Orders order) {
        // TODO : 재고 복구 로직
    }
}