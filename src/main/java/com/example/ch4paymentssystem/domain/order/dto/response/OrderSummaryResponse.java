package com.example.ch4paymentssystem.domain.order.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@AllArgsConstructor
public class OrderSummaryResponse {

    private Long orderId;
    private String orderNumber;
    private String orderStatus;
    private int totalAmount;
    private int usedPointAmount;
    private int pgAmount;
    private String paymentStatus;
    private LocalDateTime createdAt;
}