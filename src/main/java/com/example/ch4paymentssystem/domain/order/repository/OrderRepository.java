package com.example.ch4paymentssystem.domain.order.repository;

import com.example.ch4paymentssystem.domain.order.entity.Order;
import com.example.ch4paymentssystem.domain.order.entity.OrderStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OrderRepository extends JpaRepository<Order, Long> {
    Page<Order> findAllByUser_Id(Long userId, Pageable pageable);

    Page<Order> findAllByUser_IdAndOrderStatus(Long userId, OrderStatus orderStatus, Pageable pageable);
}
