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
import com.sparta.fcfsproject.ticket.dto.TicketDto;
import com.sparta.fcfsproject.ticket.service.TicketService;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class OrderService {
    private final OrderRepository orderRepository;
    private final TicketService ticketService;
    private final OrderedTicketRepository orderedTicketRepository;

    public OrderService(OrderRepository orderRepository, TicketService ticketService, OrderedTicketRepository orderedTicketRepository) {
        this.orderRepository = orderRepository;
        this.ticketService = ticketService;
        this.orderedTicketRepository = orderedTicketRepository;
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

    // 기본 주문 생성 구현 (나중에 추가할 부분)
    @Transactional
    public void createOrder(User user, OrderRequestDto orderRequestDto) {
        // 주문 티켓 리스트 가져오기
        List<OrderedTicketDto> orderedTickets = orderRequestDto.getOrderedTickets();
        // 주문 생성
        Orders orders = Orders.createPendingOrder(user.getId());
        orderRepository.save(orders);

        // 주문 처리
        for(OrderedTicketDto orderedTicketDto : orderedTickets){
            // 티켓 가져오기
            TicketDto ticket = ticketService.readTicket(orderedTicketDto.getTicketId());

            // 판매 중이 아닌 경우
            if(!"ON_SALE".equals(ticket.getStatus())){
                throw new OrderBusinessException(OrderServiceErrorCode.TICKET_NOT_ON_SALE);
            }

            // 재고 확인
            if(ticket.getStock() < orderedTicketDto.getQuantity()){
                throw new OrderBusinessException(OrderServiceErrorCode.INSUFFICIENT_STOCK);
            }

            // 오더 티켓 생성
            OrderedTicket orderedTicket = OrderedTicket.create(
                    orders.getId(),
                    ticket.getId(),
                    orderedTicketDto.getQuantity(),
                    ticket.getPrice() * orderedTicketDto.getQuantity()
            );
            orderedTicketRepository.save(orderedTicket);
        }

        // 결제 프로세스로 연결
    }
}
