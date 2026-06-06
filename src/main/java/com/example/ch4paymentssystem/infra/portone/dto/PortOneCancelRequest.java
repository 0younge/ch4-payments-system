package com.example.ch4paymentssystem.infra.portone.dto;

public record PortOneCancelRequest(
        String reason,
        String storeId
) {
}
