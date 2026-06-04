package com.example.ch4paymentssystem.domain.cart.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;

@Getter
@AllArgsConstructor
public class CartResponse {

    private final Long cartId;
    private final List<CartItemResponse> items;
    private final Integer totalAmount;
}