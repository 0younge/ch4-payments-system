package com.example.ch4paymentssystem.domain.cart.dto;

import com.example.ch4paymentssystem.domain.cart.entity.CartItem;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class CartItemResponse {

    private final Long cartItemId;
    private final Long productId;
    private final String productName;
    private final Integer price;
    private final Integer quantity;
    private final Integer itemTotalAmount;

    public static CartItemResponse from(CartItem cartItem) {
        return new CartItemResponse(
                cartItem.getId(),
                cartItem.getProduct().getId(),
                cartItem.getProduct().getName(),
                cartItem.getProduct().getPrice(),
                cartItem.getQuantity(),
                cartItem.getProduct().getPrice() * cartItem.getQuantity()
        );
    }
}