package com.example.ch4paymentssystem.domain.order.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class RestoredStockResponse {

    private Long productId;
    private String productName;
    private int restoredQuantity;
}