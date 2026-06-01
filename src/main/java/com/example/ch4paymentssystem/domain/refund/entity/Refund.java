package com.example.ch4paymentssystem.domain.refund.entity;

import com.example.ch4paymentssystem.common.BaseTimeEntity;
import com.example.ch4paymentssystem.domain.payment.entity.Payment;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@Entity
@Table(name = "refunds")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Refund extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "payment_id", nullable = false)
    private Payment payment;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private RefundStatus refundStatus;

    @Column(nullable = false)
    private Integer totalRefundAmount;

    @Column(nullable = false)
    private Integer pointRefundAmount;

    @Column(nullable = false)
    private Integer pgRefundAmount;

    @Column(nullable = false, length = 255)
    private String refundReason;

    private LocalDateTime refundedAt;

    private LocalDateTime failedAt;

    @Column(length = 255)
    private String failReason;

}
