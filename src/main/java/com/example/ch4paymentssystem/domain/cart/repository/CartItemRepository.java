package com.example.ch4paymentssystem.domain.cart.repository;

import com.example.ch4paymentssystem.domain.cart.entity.CartItem;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CartItemRepository extends JpaRepository<CartItem, Long> {
}
