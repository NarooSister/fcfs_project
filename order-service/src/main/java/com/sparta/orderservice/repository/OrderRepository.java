package com.sparta.orderservice.repository;

import com.sparta.orderservice.entity.Orders;
import org.hibernate.query.Order;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface OrderRepository extends JpaRepository<Orders, Long> {
    //List<Orders> findAllByUserId(Long userId);
    List<Orders> findAllByUsername(String username);
   // Optional<Orders> findByIdAndUserId(Long id, Long userId);
    Optional<Orders> findByIdAndUsername(Long id, String username);
}
