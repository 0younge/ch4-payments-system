package com.example.ch4paymentssystem.domain.order.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@AllArgsConstructor
public class OrderDetailResponse {

    private Long orderId;
    private String orderNumber;
    private String orderStatus;
    private int totalAmount;
    private int usedPointAmount;
    private int pgAmount;
    private int savedPointAmount;
    private LocalDateTime createdAt;

    private OrderPaymentResponse payment;
    private List<OrderDetailItemResponse> items;
}
