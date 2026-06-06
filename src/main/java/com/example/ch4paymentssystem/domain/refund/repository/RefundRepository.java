package com.example.ch4paymentssystem.domain.refund.repository;

import com.example.ch4paymentssystem.domain.refund.entity.Refund;
import com.example.ch4paymentssystem.domain.refund.entity.RefundStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface RefundRepository extends JpaRepository<Refund, Long> {

    List<Refund> findAllByPaymentIdAndRefundStatus(Long paymentId, RefundStatus refundStatus);
}
