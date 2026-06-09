package com.example.ch4paymentssystem.domain.order.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class OrderDetailItemResponse {

    private Long orderItemId;
    private Long productId;
    private String productName;
    private int productPrice;
    private int quantity;
    private int itemTotalAmount;
}
