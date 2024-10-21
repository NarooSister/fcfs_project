package com.sparta.fcfsproject.order.service;

import com.sparta.fcfsproject.auth.entity.User;
import com.sparta.fcfsproject.common.exception.OrderBusinessException;
import com.sparta.fcfsproject.common.exception.OrderServiceErrorCode;
import com.sparta.fcfsproject.order.dto.OrderDto;
import com.sparta.fcfsproject.order.entity.Orders;
import com.sparta.fcfsproject.order.repository.OrderRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class OrderService {
    private final OrderRepository orderRepository;

    public OrderService(OrderRepository orderRepository) {
        this.orderRepository = orderRepository;
    }

    public List<OrderDto> readAll(User user) {
        List<Orders> orderList = orderRepository.findAllByUserId(user.getId());
        if (orderList.isEmpty()) {
            throw new OrderBusinessException(OrderServiceErrorCode.ALL_ORDER_NOT_FOUND);
        }
        return orderList.stream()
                .map(OrderDto::new)
                .collect(Collectors.toList());
    }

    public OrderDto read(User user, Long orderId) {
        Orders order = orderRepository.findByIdAndUserId(orderId, user.getId())
                .orElseThrow(() -> new OrderBusinessException(OrderServiceErrorCode.ORDER_NOT_FOUND)
        );
        return new OrderDto(order);
    }
}
