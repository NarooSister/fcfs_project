package com.sparta.orderservice.repository;

import com.sparta.orderservice.entity.Orders;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface OrderRepository extends JpaRepository<Orders, Long> {
    List<Orders> findAllByUsername(String username);

    Optional<Orders> findByIdAndUsername(Long id, String username);
}
