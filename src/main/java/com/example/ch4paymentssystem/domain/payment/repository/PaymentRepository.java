package com.example.ch4paymentssystem.domain.payment.repository;

import com.example.ch4paymentssystem.domain.payment.entity.Payment;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PaymentRepository extends JpaRepository<Payment, Long> {
}
