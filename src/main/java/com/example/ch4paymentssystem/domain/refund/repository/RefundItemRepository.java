package com.example.ch4paymentssystem.domain.refund.repository;

import com.example.ch4paymentssystem.domain.refund.entity.RefundItem;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RefundItemRepository extends JpaRepository<RefundItem, Long> {
}
