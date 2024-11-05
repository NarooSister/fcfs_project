package com.sparta.orderservice.unit_test;

import com.sparta.orderservice.client.TicketClient;
import com.sparta.orderservice.dto.OrderDto;
import com.sparta.orderservice.dto.OrderRequestDto;
import com.sparta.orderservice.dto.OrderedTicketDto;
import com.sparta.orderservice.dto.TicketDto;
import com.sparta.orderservice.entity.OrderedTicket;
import com.sparta.orderservice.entity.Orders;
import com.sparta.orderservice.event.StockDecrEvent;
import com.sparta.orderservice.event.StockIncrEvent;
import com.sparta.orderservice.exception.OrderBusinessException;
import com.sparta.orderservice.exception.OrderServiceErrorCode;
import com.sparta.orderservice.repository.OrderRepository;
import com.sparta.orderservice.repository.OrderedTicketRepository;
import com.sparta.orderservice.service.OrderConfirmationService;
import com.sparta.orderservice.service.OrderService;
import com.sparta.orderservice.service.RefundService;
import com.sparta.orderservice.toss.PaymentResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.util.concurrent.SettableListenableFuture;

import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class OrderServiceTest {
    private static final Logger logger = LoggerFactory.getLogger(OrderServiceTest.class);

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private OrderedTicketRepository orderedTicketRepository;

    @Mock
    private TicketClient ticketClient;

    @Mock
    private RefundService refundService;

    @InjectMocks
    private OrderService orderService;

    @Mock
    private OrderConfirmationService orderConfirmationService;

    @Mock
    private RedisTemplate<String, Integer> redisTemplate;

    @Mock
    private ValueOperations<String, Integer> valueOperations;

    @Mock
    private KafkaTemplate<String, StockDecrEvent> kafkaDecrTemplate;

    @Mock
    private KafkaTemplate<String, StockIncrEvent> kafkaIncrTemplate;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
    }

    // readAllOrders
    @Test
    @DisplayName("주문이 없을 때 예외가 발생하는지 확인")
    void readAllOrders_NoOrders_ThrowsException() {
        when(orderRepository.findAllByUsername("username")).thenReturn(Collections.emptyList());

        OrderBusinessException exception = assertThrows(OrderBusinessException.class, () -> {
            orderService.readAllOrdersByUser("username");
        });

        assertEquals(OrderServiceErrorCode.ALL_ORDER_NOT_FOUND, exception.getErrorCode());
    }

    @Test
    @DisplayName("주문이 있을 경우 정상적으로 반환하는지 확인")
    void readAllOrders_HasOrders_ReturnsOrderList() {
        Orders order = new Orders("username");
        when(orderRepository.findAllByUsername("username")).thenReturn(List.of(order));

        List<OrderDto> result = orderService.readAllOrdersByUser("username");

        assertNotNull(result);
        assertEquals(1, result.size());
        verify(orderRepository, times(1)).findAllByUsername("username");
    }

    // readOrder
    @Test
    @DisplayName("주문이 존재하지 않을 경우 예외 발생")
    void readOrder_OrderNotFound_ThrowsException() {
        Long orderId = 1L;
        when(orderRepository.findByIdAndUsername(orderId, "username")).thenReturn(Optional.empty());

        OrderBusinessException exception = assertThrows(OrderBusinessException.class, () -> {
            orderService.readOrderByUser("username", orderId);
        });

        assertEquals(OrderServiceErrorCode.ORDER_NOT_FOUND, exception.getErrorCode());
    }


    @Test
    @DisplayName("유효한 주문 ID로 조회 시 올바른 OrderDto 반환")
    void readOrder_ValidOrder_ReturnsOrderDto() {
        Long orderId = 1L;
        Orders order = new Orders("username");
        when(orderRepository.findByIdAndUsername(orderId, "username")).thenReturn(Optional.of(order));

        OrderDto result = orderService.readOrderByUser("username", orderId);

        assertNotNull(result);
        verify(orderRepository, times(1)).findByIdAndUsername(orderId, "username");
    }

    // createOrder
    @Test
    @DisplayName("유효하지 않은 티켓 ID로 주문 시 예외 발생")
    void createOrder_InvalidTicketId_ThrowsException() {
        Long ticketId = 1L;
        OrderRequestDto orderRequestDto = new OrderRequestDto();
        orderRequestDto.setOrderedTickets(List.of(new OrderedTicketDto(ticketId, 1)));

        when(ticketClient.getTicketById(ticketId)).thenReturn(null); // Feign Client Mock
        OrderBusinessException exception = assertThrows(OrderBusinessException.class, () -> {
            orderService.createOrder("username", orderRequestDto);
        });

        assertEquals(OrderServiceErrorCode.INVALID_TICKET, exception.getErrorCode());
    }

    @Test
    @DisplayName("티켓 상태가 '판매 중'이 아닐 경우 예외 발생")
    void createOrder_TicketNotOnSale_ThrowsException() {
        Long ticketId = 1L;
        TicketDto ticketDto = new TicketDto(ticketId, "SOLD_OUT", 10, 100, LocalDate.now().plusDays(30));

        OrderRequestDto orderRequestDto = new OrderRequestDto();
        orderRequestDto.setOrderedTickets(List.of(new OrderedTicketDto(ticketId, 1)));

        when(ticketClient.getTicketById(ticketId)).thenReturn(ticketDto);

        OrderBusinessException exception = assertThrows(OrderBusinessException.class, () -> {
            orderService.createOrder("username", orderRequestDto);
        });

        assertEquals(OrderServiceErrorCode.INVALID_TICKET, exception.getErrorCode());
    }

    @Test
    @DisplayName("주문 수량이 재고보다 많을 경우 예외 발생")
    void createOrder_InsufficientStock_ThrowsException() {
        Long ticketId = 1L;
        TicketDto ticketDto = new TicketDto(ticketId, "ON_SALE", 1, 100, LocalDate.now().plusDays(30));

        OrderRequestDto orderRequestDto = new OrderRequestDto();
        orderRequestDto.setOrderedTickets(List.of(new OrderedTicketDto(ticketId, 2)));

        when(ticketClient.getTicketById(ticketId)).thenReturn(ticketDto);

        OrderBusinessException exception = assertThrows(OrderBusinessException.class, () -> {
            orderService.createOrder("username", orderRequestDto);
        });

        assertEquals(OrderServiceErrorCode.INVALID_TICKET, exception.getErrorCode());
    }
    @Test
    @DisplayName("유효한 주문 요청 시 주문이 정상적으로 생성됨을 확인")
    void createOrder_WithValidRequest_CreatesOrderWithoutErrors() {
        // Given
        String username = "username";
        Long ticketId = 1L;
        OrderRequestDto orderRequestDto = new OrderRequestDto();
        orderRequestDto.setOrderedTickets(List.of(new OrderedTicketDto(ticketId, 1)));

        // 티켓 정보 설정
        TicketDto ticketDto = new TicketDto(ticketId, "ON_SALE", 10, 100, LocalDate.now().plusDays(30));
        when(ticketClient.getTicketById(ticketId)).thenReturn(ticketDto);

        // Redis Mock 설정 - 충분한 재고가 있는 경우
        when(redisTemplate.opsForValue().get("stock:" + ticketId)).thenReturn(10);
        when(redisTemplate.opsForValue().decrement("stock:" + ticketId, 1)).thenReturn(9L);

        // 주문 저장 Mock
        Orders order = new Orders(username);
        when(orderRepository.save(any(Orders.class))).thenReturn(order);

        // 결제 검증 로직에 대한 Mock 설정 (void 메서드)
        doNothing().when(orderConfirmationService).confirmOrderAndPayment(any(Orders.class), any(PaymentResponse.class));

        // 주문 생성 로직 실행
        orderService.createOrder(username, orderRequestDto);

        // 저장 호출 검증
        verify(orderRepository, times(2)).save(any(Orders.class));
        verify(orderedTicketRepository, times(1)).save(any(OrderedTicket.class));

        // Kafka 이벤트 전송 확인
        verify(kafkaDecrTemplate, times(1)).send(anyString(), any(StockDecrEvent.class));
    }

    // cancelOrder
    @Test
    @DisplayName("주문이 존재하지 않을 경우 예외 발생")
    void cancelOrder_OrderNotFound_ThrowsException() {
        Long orderId = 1L;
        when(orderRepository.findByIdAndUsername(orderId, "username")).thenReturn(Optional.empty());

        OrderBusinessException exception = assertThrows(OrderBusinessException.class, () -> {
            orderService.cancelOrder("username", orderId);
        });

        assertEquals(OrderServiceErrorCode.ORDER_NOT_FOUND, exception.getErrorCode());
    }

    @Test
    @DisplayName("유효한 주문 ID로 취소 시 정상적으로 취소 처리되는지 확인")
    void cancelOrder_ValidOrder_CancelsOrderSuccessfully() {
        // Mock 설정
        Long orderId = 1L;
        Orders order = new Orders("username");
        order.confirmOrder();
        when(orderRepository.findByIdAndUsername(orderId, "username")).thenReturn(Optional.of(order));

        Long ticketId = 1L;
        TicketDto ticketDto = new TicketDto(ticketId, "ON_SALE", 10, 100, LocalDate.now().plusDays(30));
        when(ticketClient.getTicketById(ticketId)).thenReturn(ticketDto);

        OrderedTicket orderedTicket = OrderedTicket.createPending(orderId, ticketId, 1, 100);
        when(orderedTicketRepository.findByOrderId(orderId)).thenReturn(List.of(orderedTicket));

        // Redis Mock 설정
        when(redisTemplate.opsForValue().increment(anyString(), anyLong())).thenReturn(1L);

        // 주문 취소 호출
        orderService.cancelOrder("username", orderId);

        // 검증
        verify(orderRepository, times(1)).save(order);
        verify(orderedTicketRepository, times(1)).saveAll(anyList());

        // 상태 확인
        assertEquals(Orders.OrderStatus.CANCELED, order.getStatus());
        assertEquals(OrderedTicket.Status.CANCELED, orderedTicket.getStatus());

        // Redis 호출 검증
        verify(redisTemplate.opsForValue(), times(1)).increment(anyString(), eq(1L));
    }
}

