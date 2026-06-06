package com.example.ch4paymentssystem.domain.order.dto.request;

import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class CancelOrderRequest {

    private String reason;
}