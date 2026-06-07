package com.example.ch4paymentssystem.infra.portone.dto;

public record PortOneCancelRequest(
        String reason,
        Integer amount,
        String storeId
) {
}
