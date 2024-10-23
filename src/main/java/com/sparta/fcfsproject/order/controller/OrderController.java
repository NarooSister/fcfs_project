package com.sparta.fcfsproject.order.controller;

import com.sparta.fcfsproject.auth.config.AuthFacade;
import com.sparta.fcfsproject.auth.entity.User;
import com.sparta.fcfsproject.order.dto.OrderDto;
import com.sparta.fcfsproject.order.dto.OrderRequestDto;
import com.sparta.fcfsproject.order.service.OrderService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/orders")
public class OrderController {
    private final OrderService orderService;

    public OrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    @GetMapping
    public ResponseEntity<List<OrderDto>> readAllOrders(){
        User user = AuthFacade.getCurrentUser();
        List<OrderDto> orderList = orderService.readAllOrders(user);
        return ResponseEntity.ok(orderList);
    }

    @GetMapping("/{orderId}")
    public ResponseEntity<OrderDto> readOrder(@PathVariable("orderId") Long orderId){
        User user = AuthFacade.getCurrentUser();
        OrderDto order = orderService.readOrder(user, orderId);
        return ResponseEntity.ok(order);
    }

    @PostMapping
    public ResponseEntity<String> createOrder(@RequestBody OrderRequestDto orderRequestDto){
        User user = AuthFacade.getCurrentUser();
        orderService.createOrder(user, orderRequestDto);
        return ResponseEntity.ok("티켓 주문이 완료되었습니다.");
    }

    @PostMapping("/{orderId}/cancel")
    public ResponseEntity<String> cancelOrder(@PathVariable("orderId") Long orderId){
        User user = AuthFacade.getCurrentUser();
        orderService.cancelOrder(user, orderId);
        return ResponseEntity.ok("티켓 주문이 취소되었습니다.");
    }
}
