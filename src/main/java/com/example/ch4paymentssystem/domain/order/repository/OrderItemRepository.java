package com.example.ch4paymentssystem.domain.order.repository;

import com.example.ch4paymentssystem.domain.order.entity.OrderItem;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OrderItemRepository extends JpaRepository<OrderItem, Long> {
}
