package com.sparta.orderservice.controller;

import com.sparta.orderservice.dto.OrderDto;
import com.sparta.orderservice.dto.OrderRequestDto;
import com.sparta.orderservice.dto.UserInfo;
import com.sparta.orderservice.service.OrderService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/order")
public class OrderController {
    private final OrderService orderService;

    public OrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    @GetMapping
    public ResponseEntity<List<OrderDto>> readAllOrdersByUser(UserInfo userInfo){
        List<OrderDto> orderList = orderService.readAllOrdersByUser(userInfo.username());
        return ResponseEntity.ok(orderList);
    }
    @GetMapping("/{orderId}")
    public ResponseEntity<OrderDto> readOrderByUser(@PathVariable("orderId") Long orderId, UserInfo userInfo){
        OrderDto order = orderService.readOrderByUser(userInfo.username(), orderId);
        return ResponseEntity.ok(order);
    }

    @PostMapping("/pending")
    public ResponseEntity<String> createPendingOrder(@RequestBody OrderRequestDto orderRequestDto, UserInfo userInfo){
        String pendingOrderId = orderService.createPendingOrder(userInfo.username(), orderRequestDto);
        return ResponseEntity.ok("예비 주문이 생성되었습니다. 예비 주문 ID: " + pendingOrderId);
    }

    @PostMapping("/complete")
    public ResponseEntity<String> createOrder(@RequestBody OrderRequestDto orderRequestDto, UserInfo userInfo){
        orderService.createOrder(userInfo.username(), orderRequestDto);
        return ResponseEntity.ok("티켓 주문이 완료되었습니다.");
    }

    @PostMapping("/{orderId}/cancel")
    public ResponseEntity<String> cancelOrder(@PathVariable("orderId") Long orderId, UserInfo userInfo){
        orderService.cancelOrder(userInfo.username(), orderId);
        return ResponseEntity.ok("티켓 주문이 취소되었습니다.");
    }
}
