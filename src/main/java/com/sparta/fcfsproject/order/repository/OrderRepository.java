package com.sparta.fcfsproject.order.repository;

import com.sparta.fcfsproject.order.entity.Orders;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface OrderRepository extends JpaRepository<Orders, Long> {
    List<Orders> findAllByUserId(Long userId);

    Optional<Orders> findByIdAndUserId(Long id, Long userId);
}
