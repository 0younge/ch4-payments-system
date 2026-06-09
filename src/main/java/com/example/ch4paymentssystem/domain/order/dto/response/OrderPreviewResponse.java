package com.example.ch4paymentssystem.domain.order.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;

@Getter
@AllArgsConstructor
public class OrderPreviewResponse {

    private List<OrderPreviewItemResponse> items;
    private int totalAmount;
}