package com.example.ch4paymentssystem.domain.payment.port;

public interface PaymentGateway {

    PaymentGatewayResponse getPayment(String portonePaymentId);

    void cancelPayment(String portonePaymentId, String reason);
}
