package com.example.ch4paymentssystem.domain.cart.repository;

import com.example.ch4paymentssystem.domain.cart.entity.Cart;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CartRepository extends JpaRepository<Cart, Long> {
}
