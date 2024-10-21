package com.sparta.fcfsproject.order.controller;

import com.sparta.fcfsproject.auth.config.AuthFacade;
import com.sparta.fcfsproject.auth.entity.User;
import com.sparta.fcfsproject.order.dto.OrderDto;
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
    public ResponseEntity<List<OrderDto>> readAll(){
        User user = AuthFacade.getCurrentUser();
        List<OrderDto> orderList = orderService.readAll(user);
        return ResponseEntity.ok(orderList);
    }
    @GetMapping("/{orderId}")
    public ResponseEntity<OrderDto> readAll(@PathVariable("orderId") Long orderId){
        User user = AuthFacade.getCurrentUser();
        OrderDto order = orderService.read(user, orderId);
        return ResponseEntity.ok(order);
    }
}
