package com.example.ch4paymentssystem.domain.payment.dto;

public record PaymentConfigResponse(
        String storeId,
        String channelKey
) {
}
