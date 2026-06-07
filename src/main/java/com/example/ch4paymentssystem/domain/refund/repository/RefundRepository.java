package com.example.ch4paymentssystem.domain.refund.repository;

import com.example.ch4paymentssystem.domain.refund.entity.Refund;
import com.example.ch4paymentssystem.domain.refund.entity.RefundStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface RefundRepository extends JpaRepository<Refund, Long> {

    List<Refund> findAllByPaymentIdAndRefundStatus(Long paymentId, RefundStatus refundStatus);

    @Query("select r from Refund r join fetch r.payment p join fetch p.order o join fetch o.user where r.id = :refundId")
    Optional<Refund> findByIdWithPaymentOrderUser(@Param("refundId") Long refundId);
}
