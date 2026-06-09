package com.example.ch4paymentssystem.domain.payment.dto;

import com.example.ch4paymentssystem.domain.order.entity.Order;
import com.example.ch4paymentssystem.domain.payment.entity.Payment;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@AllArgsConstructor
public class PaymentConfirmResponse {

    private Long paymentId;
    private Long orderId;
    private String portonePaymentId;
    private int totalAmount;
    private int usedPointAmount;
    private int pgAmount;
    private int earnedPointAmount;
    private String paymentStatus;
    private String orderStatus;
    private LocalDateTime paidAt;

    public static PaymentConfirmResponse from(Payment payment) {
        Order order = payment.getOrder();

        return new PaymentConfirmResponse(
                payment.getId(),
                order.getId(),
                payment.getPortonePaymentId(),
                order.getTotalProductAmount(),
                order.getUsedPointAmount(),
                payment.getPgAmount(),
                order.getEarnedPointAmount(),
                payment.getPaymentStatus().name(),
                order.getOrderStatus().name(),
                payment.getPaidAt()
        );
    }
}
