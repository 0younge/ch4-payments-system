package com.example.ch4paymentssystem.domain.order.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;

@Getter
@AllArgsConstructor
public class CreateOrderResponse {

    private Long orderId;
    private String orderNumber;
    private String orderStatus;
    private Long paymentId;
    private String portonePaymentId;
    private String paymentStatus;
    private int totalAmount;
    private int usedPointAmount;
    private int pgAmount;
    private List<CreateOrderItemResponse> items;
}
