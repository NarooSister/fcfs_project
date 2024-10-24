package com.sparta.fcfsproject.unitTest;

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
import com.sparta.fcfsproject.order.service.OrderService;
import com.sparta.fcfsproject.order.service.RefundPolicy;
import com.sparta.fcfsproject.order.service.RefundService;
import com.sparta.fcfsproject.ticket.entity.Ticket;
import com.sparta.fcfsproject.ticket.repository.TicketRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class OrderServiceTest {
    private static final Logger logger = LoggerFactory.getLogger(OrderServiceTest.class);

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private OrderedTicketRepository orderedTicketRepository;

    @Mock
    private TicketRepository ticketRepository;

    @Mock
    private RefundService refundService;
    @InjectMocks
    private OrderService orderService;

    private User user;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        this.user = new User(1L, "username", "email@example.com", "encodedPassword", "name", "010-1234-5678", "address", "ROLE_USER");
    }

    // readAllOrders
    @Test
    @DisplayName("주문이 없을 때 예외가 발생하는지 확인")
    void readAllOrders_NoOrders_ThrowsException() {
        when(orderRepository.findAllByUserId(user.getId())).thenReturn(Collections.emptyList());

        OrderBusinessException exception = assertThrows(OrderBusinessException.class, () -> {
            orderService.readAllOrders(user);
        });

        assertEquals(OrderServiceErrorCode.ALL_ORDER_NOT_FOUND, exception.getErrorCode());
    }

    @Test
    @DisplayName("주문이 있을 경우 정상적으로 반환하는지 확인")
    void readAllOrders_HasOrders_ReturnsOrderList() {
        Orders order = new Orders(user.getId());
        when(orderRepository.findAllByUserId(user.getId())).thenReturn(List.of(order));

        List<OrderDto> result = orderService.readAllOrders(user);

        assertNotNull(result);
        assertEquals(1, result.size());
        verify(orderRepository, times(1)).findAllByUserId(user.getId());
    }

    // readOrder
    @Test
    @DisplayName("주문이 존재하지 않을 경우 예외 발생")
    void readOrder_OrderNotFound_ThrowsException() {
        Long orderId = 1L;
        when(orderRepository.findByIdAndUserId(orderId, user.getId())).thenReturn(Optional.empty());

        OrderBusinessException exception = assertThrows(OrderBusinessException.class, () -> {
            orderService.readOrder(user, orderId);
        });

        assertEquals(OrderServiceErrorCode.ORDER_NOT_FOUND, exception.getErrorCode());
    }

    @Test
    @DisplayName("유효한 주문 ID로 조회 시 올바른 OrderDto 반환")
    void readOrder_ValidOrder_ReturnsOrderDto() {
        Long orderId = 1L;
        Orders order = new Orders(user.getId());
        when(orderRepository.findByIdAndUserId(orderId, user.getId())).thenReturn(Optional.of(order));

        OrderDto result = orderService.readOrder(user, orderId);

        assertNotNull(result);
        verify(orderRepository, times(1)).findByIdAndUserId(orderId, user.getId());
    }

    // createOrder
    @Test
    @DisplayName("유효하지 않은 티켓 ID로 주문 시 예외 발생")
    void createOrder_InvalidTicketId_ThrowsException() {
        Long ticketId = 1L;
        OrderRequestDto orderRequestDto = new OrderRequestDto();
        orderRequestDto.setOrderedTickets(List.of(new OrderedTicketDto(ticketId, 1)));

        when(ticketRepository.findById(ticketId)).thenReturn(Optional.empty());

        OrderBusinessException exception = assertThrows(OrderBusinessException.class, () -> {
            orderService.createOrder(user, orderRequestDto);
        });

        assertEquals(OrderServiceErrorCode.TICKET_NOT_FOUND, exception.getErrorCode());
    }

    @Test
    @DisplayName("티켓 상태가 '판매 중'이 아닐 경우 예외 발생")
    void createOrder_TicketNotOnSale_ThrowsException() {
        Long ticketId = 1L;
        Ticket ticket = new Ticket(); // 필요한 필드를 설정하세요
        //ticket.setStatus(Ticket.Status.SOLD_OUT);

        OrderRequestDto orderRequestDto = new OrderRequestDto();
        orderRequestDto.setOrderedTickets(List.of(new OrderedTicketDto(ticketId, 1)));

        when(ticketRepository.findById(ticketId)).thenReturn(Optional.of(ticket));

        OrderBusinessException exception = assertThrows(OrderBusinessException.class, () -> {
            orderService.createOrder(user, orderRequestDto);
        });

        assertEquals(OrderServiceErrorCode.TICKET_NOT_ON_SALE, exception.getErrorCode());
    }

    @Test
    @DisplayName("주문 수량이 재고보다 많을 경우 예외 발생")
    void createOrder_InsufficientStock_ThrowsException() {
        Long ticketId = 1L;

        // Ticket 객체를 생성할 때 필요한 필드를 초기화
        Ticket ticket = new Ticket(ticketId, "Concert Ticket", 100, 1, "This is a concert ticket.", LocalDate.now().plusDays(30), Ticket.Status.ON_SALE, Ticket.Type.GENERAL);

        OrderRequestDto orderRequestDto = new OrderRequestDto();
        orderRequestDto.setOrderedTickets(List.of(new OrderedTicketDto(ticketId, 2))); // 수량을 2로 설정

        when(ticketRepository.findById(ticketId)).thenReturn(Optional.of(ticket));

        OrderBusinessException exception = assertThrows(OrderBusinessException.class, () -> {
            orderService.createOrder(user, orderRequestDto);
        });

        assertEquals(OrderServiceErrorCode.INSUFFICIENT_STOCK, exception.getErrorCode());
    }

    @Test
    @DisplayName("유효한 주문 요청을 통해 주문이 성공적으로 생성되는지 확인")
    void createOrder_ValidRequest_CreatesOrderSuccessfully() {
        Long ticketId = 1L;

        // Ticket 객체를 생성하고 필요한 필드를 설정합니다.
        Ticket ticket = new Ticket(ticketId, "Concert Ticket", 100, 10, "This is a concert ticket.", LocalDate.now().plusDays(30), Ticket.Status.ON_SALE, Ticket.Type.GENERAL);

        OrderRequestDto orderRequestDto = new OrderRequestDto();
        orderRequestDto.setOrderedTickets(List.of(new OrderedTicketDto(ticketId, 1))); // 수량을 1로 설정

        // 티켓 리포지토리에서 티켓을 찾을 수 있도록 설정
        when(ticketRepository.findById(ticketId)).thenReturn(Optional.of(ticket));

        // 주문 생성 메서드 호출
        orderService.createOrder(user, orderRequestDto);

        // 검증: Orders와 OrderedTicket이 정상적으로 저장되었는지 확인
        verify(orderRepository, times(1)).save(any(Orders.class));
        verify(orderedTicketRepository, times(1)).save(any(OrderedTicket.class));

        // 추가 검증: 주문의 총 가격이 정확히 계산되었는지 확인 (추가 필요)
    }

    // cancelOrder
    @Test
    @DisplayName("주문이 존재하지 않을 경우 예외 발생")
    void cancelOrder_OrderNotFound_ThrowsException() {
        Long orderId = 1L;
        when(orderRepository.findByIdAndUserId(orderId, user.getId())).thenReturn(Optional.empty());

        OrderBusinessException exception = assertThrows(OrderBusinessException.class, () -> {
            orderService.cancelOrder(user, orderId);
        });

        assertEquals(OrderServiceErrorCode.ORDER_NOT_FOUND, exception.getErrorCode());
    }

    @Test
    @DisplayName("유효한 주문 ID로 취소 시 정상적으로 취소 처리되는지 확인")
    void cancelOrder_ValidOrder_CancelsOrderSuccessfully() {
        Long orderId = 1L;
        Orders order = new Orders(user.getId());
        when(orderRepository.findByIdAndUserId(orderId, user.getId())).thenReturn(Optional.of(order));

        Long ticketId = 1L; // 티켓 ID 설정
        // Mock Ticket 객체를 생성하여 ticketRepository에 설정
        Ticket ticket = new Ticket(ticketId, "Concert Ticket", 100, 10, "This is a concert ticket.", LocalDate.now().plusDays(30), Ticket.Status.ON_SALE, Ticket.Type.GENERAL);
        when(ticketRepository.findById(ticketId)).thenReturn(Optional.of(ticket));

        OrderedTicket orderedTicket = OrderedTicket.createPending(ticketId, orderId, 1, 100);

        List<OrderedTicket> orderedTickets = List.of(orderedTicket);
        when(orderedTicketRepository.findByOrderId(orderId)).thenReturn(orderedTickets);

        // 주문 취소 메서드 호출
        orderService.cancelOrder(user, orderId);

        verify(orderRepository, times(1)).save(order); // 주문 저장 호출 확인
        assertEquals(OrderedTicket.Status.CANCELED, orderedTicket.getStatus()); // 상태가 취소로 변경되었는지 확인
        verify(ticketRepository, times(1)).save(ticket); // 티켓 저장 호출 확인
    }

    @Test
    @DisplayName("주문 생성 시 재고가 제대로 차감되는지 확인")
    void createOrder_StockReduction_ValidRequest() {
        Long ticketId = 1L;
        Ticket ticket = new Ticket(ticketId, "Concert Ticket", 100, 10, "This is a concert ticket.", LocalDate.now().plusDays(30), Ticket.Status.ON_SALE, Ticket.Type.GENERAL);

        OrderRequestDto orderRequestDto = new OrderRequestDto();
        orderRequestDto.setOrderedTickets(List.of(new OrderedTicketDto(ticketId, 1))); // 수량을 1로 설정

        when(ticketRepository.findById(ticketId)).thenReturn(Optional.of(ticket));

        // 주문 생성 메서드 호출
        orderService.createOrder(user, orderRequestDto);

        // 재고가 1만큼 차감되었는지 확인
        assertEquals(9, ticket.getStock());
    }
}
