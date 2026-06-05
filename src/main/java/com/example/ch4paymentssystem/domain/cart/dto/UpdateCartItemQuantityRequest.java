package com.example.ch4paymentssystem.domain.cart.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class UpdateCartItemQuantityRequest {

    @NotNull
    @Min(1)
    private Integer quantity;
}
