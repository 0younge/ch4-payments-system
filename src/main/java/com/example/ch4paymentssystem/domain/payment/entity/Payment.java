package com.example.ch4paymentssystem.domain.payment.entity;

import com.example.ch4paymentssystem.common.BaseTimeEntity;
import com.example.ch4paymentssystem.domain.order.entity.Order;
import com.example.ch4paymentssystem.global.exception.BusinessException;
import com.example.ch4paymentssystem.global.exception.ErrorCode;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@Entity
@Table(name = "payments")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Payment extends BaseTimeEntity {

    private static final String DEFAULT_PG_PROVIDER = "KG_INICIS";

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id", nullable = false, unique = true)
    private Order order;

    @Column(nullable = false, unique = true, length = 100)
    private String portonePaymentId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private PaymentStatus paymentStatus;

    @Column(nullable = false, length = 50)
    private String pgProvider;

    @Column(nullable = false)
    private Integer pgAmount;

    private LocalDateTime paidAt;

    private LocalDateTime failedAt;

    @Column(length = 255)
    private String failReason;

    public static Payment create(
            Order order,
            String portonePaymentId,
            int pgAmount
    ) {
        if (pgAmount < 0) {
            throw new BusinessException(ErrorCode.INVALID_PAYMENT_REQUEST);
        }

        Payment payment = new Payment();
        payment.order = order;
        payment.portonePaymentId = portonePaymentId;
        payment.paymentStatus = PaymentStatus.READY;
        payment.pgProvider = DEFAULT_PG_PROVIDER;
        payment.pgAmount = pgAmount;
        return payment;
    }

    public void pay() {
        if (this.paymentStatus != PaymentStatus.READY) {
            throw new BusinessException(ErrorCode.INVALID_PAYMENT_STATUS);
        }

        this.paymentStatus = PaymentStatus.PAID;
        this.paidAt = LocalDateTime.now();
    }

    public void fail(String failReason) {
        if (this.paymentStatus != PaymentStatus.READY) {
            throw new BusinessException(ErrorCode.INVALID_PAYMENT_STATUS);
        }

        this.paymentStatus = PaymentStatus.FAILED;
        this.failedAt = LocalDateTime.now();
        this.failReason = failReason;
    }

}
