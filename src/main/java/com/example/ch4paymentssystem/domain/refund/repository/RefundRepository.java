package com.example.ch4paymentssystem.domain.refund.repository;

import com.example.ch4paymentssystem.domain.refund.entity.Refund;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RefundRepository extends JpaRepository<Refund, Long> {
}
