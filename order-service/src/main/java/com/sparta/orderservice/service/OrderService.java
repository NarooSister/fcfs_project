package com.sparta.orderservice.service;


import com.sparta.orderservice.client.TicketClient;
import com.sparta.orderservice.dto.OrderDto;
import com.sparta.orderservice.dto.OrderRequestDto;
import com.sparta.orderservice.dto.OrderedTicketDto;
import com.sparta.orderservice.dto.TicketDto;
import com.sparta.orderservice.entity.OrderedTicket;
import com.sparta.orderservice.entity.Orders;
import com.sparta.orderservice.exception.OrderBusinessException;
import com.sparta.orderservice.exception.OrderServiceErrorCode;
import com.sparta.orderservice.repository.OrderRepository;
import com.sparta.orderservice.repository.OrderedTicketRepository;

import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class OrderService {
    private final OrderRepository orderRepository;
    private final OrderedTicketRepository orderedTicketRepository;
    private final TicketClient ticketClient;
   private final RefundService refundService;

    public OrderService(OrderRepository orderRepository, OrderedTicketRepository orderedTicketRepository, RefundService refundService, TicketClient ticketClient) {
        this.orderRepository = orderRepository;
        this.orderedTicketRepository = orderedTicketRepository;
        this.refundService = refundService;
        this.ticketClient = ticketClient;
    }

    // 사용자의 모든 주문 가져오기
    public List<OrderDto> readAllOrders(String username) {
        List<Orders> orderList = orderRepository.findAllByUsername(username);
        if (orderList.isEmpty()) {
            throw new OrderBusinessException(OrderServiceErrorCode.ALL_ORDER_NOT_FOUND);
        }
        return orderList.stream()
                .map(OrderDto::new)
                .collect(Collectors.toList());
    }

    // 사용자의 주문 상세 내역 가져오기
    public OrderDto readOrder(String username, Long orderId) {
        Orders order = orderRepository.findByIdAndUsername(orderId, username)
                .orElseThrow(() -> new OrderBusinessException(OrderServiceErrorCode.ORDER_NOT_FOUND)
                );
        return new OrderDto(order);
    }

    // 기본 주문 생성 구현 (TODO : 주문 생성 나중에 추가)
    @Transactional
    public void createOrder(String username, OrderRequestDto orderRequestDto) {
        // 주문 티켓 리스트 가져오기
        List<OrderedTicketDto> orderedTickets = orderRequestDto.getOrderedTickets();
        // 주문 생성
        Orders order = new Orders(username);
        orderRepository.save(order);

        // 주문 처리
        for (OrderedTicketDto orderedTicketDto : orderedTickets) {
            // 티켓 가져오기
            TicketDto ticket = ticketClient.getTicketById(orderedTicketDto.getTicketId());
            if (ticket == null) {
                throw new OrderBusinessException(OrderServiceErrorCode.TICKET_NOT_FOUND);
            }
            // 판매 중이 아닌 경우
            if (!"ON_SALE".equals(ticket.status())) {
                throw new OrderBusinessException(OrderServiceErrorCode.TICKET_NOT_ON_SALE);
            }
            if (ticket.stock() < orderedTicketDto.getQuantity()) {
                throw new OrderBusinessException(OrderServiceErrorCode.INSUFFICIENT_STOCK);
            }

            // 오더 티켓 생성
            OrderedTicket orderedTicket = OrderedTicket.createPending(
                    order.getId(),
                    ticket.id(),
                    orderedTicketDto.getQuantity(),
                    ticket.price() * orderedTicketDto.getQuantity()
            );
            orderedTicketRepository.save(orderedTicket);

            order.updateTotalPrice(orderedTicket.getPrice());
        }
        orderRepository.save(order);
        // TODO : 결제 프로세스로 연결
    }

    // 주문 취소 로직
    public void cancelOrder(String username, Long orderId) {
        // 사용자와 주문 아이디로 주문 찾기
        Orders order = orderRepository.findByIdAndUsername(orderId, username)
                .orElseThrow(() -> new OrderBusinessException(OrderServiceErrorCode.ORDER_NOT_FOUND));

        List<OrderedTicket> orderedTickets = orderedTicketRepository.findByOrderId(orderId);

        for (OrderedTicket orderedTicket : orderedTickets) {
            // 티켓 가져오기
            TicketDto ticket = ticketClient.getTicketById(orderedTicket.getTicketId());
            if (ticket == null) {
                throw new OrderBusinessException(OrderServiceErrorCode.TICKET_NOT_FOUND);
            }

            // 티켓 관람일 기준으로 환불 금액 계산
            double refundAmount = refundService.calculateRefund(ticket, orderedTicket);

            // 환불 처리 (결제 시스템)
            processRefund(refundAmount);

            // 티켓 취소 처리
            orderedTicket.cancel();

            // 재고 복구
            ticketClient.restoreStock(ticket.id(), orderedTicket.getQuantity());
        }
        orderedTicketRepository.saveAll(orderedTickets);
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