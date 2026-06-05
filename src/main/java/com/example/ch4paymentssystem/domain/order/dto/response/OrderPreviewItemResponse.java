package com.example.ch4paymentssystem.domain.order.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class OrderPreviewItemResponse {

    private Long cartItemId;
    private Long productId;
    private String productName;
    private int price;
    private int quantity;
    private int stockQuantity;
    private int itemTotalAmount;
}