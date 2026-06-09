package com.example.ch4paymentssystem.domain.payment.port;

public record PaymentGatewayResponse(
        String id,
        String status,
        int totalAmount
) {
}
