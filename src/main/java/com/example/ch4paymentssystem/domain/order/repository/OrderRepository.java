package com.example.ch4paymentssystem.domain.order.repository;

import com.example.ch4paymentssystem.domain.order.entity.Order;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OrderRepository extends JpaRepository<Order, Long> {
}
