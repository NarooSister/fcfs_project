package com.sparta.fcfsproject.order.repository;

import com.sparta.fcfsproject.order.entity.Wishlist;
import org.springframework.data.jpa.repository.JpaRepository;

public interface WishlistRepository extends JpaRepository<Wishlist, Long> {
}
