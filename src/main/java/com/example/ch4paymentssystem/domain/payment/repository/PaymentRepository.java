package com.example.ch4paymentssystem.domain.payment.repository;

import com.example.ch4paymentssystem.domain.payment.entity.Payment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface PaymentRepository extends JpaRepository<Payment, Long> {

    Optional<Payment> findByOrderId(Long orderId);

    Optional<Payment> findByPortonePaymentId(String portonePaymentId);

    @Query("select p from Payment p join fetch p.order o join fetch o.user where p.order.id = :orderId")
    Optional<Payment> findByOrderIdWithOrderAndUser(@Param("orderId") Long orderId);

    @Query("select p from Payment p join fetch p.order o join fetch o.user where p.id = :paymentId")
    Optional<Payment> findByIdWithOrderAndUser(@Param("paymentId") Long paymentId);
}
