package com.example.ch4paymentssystem.domain.refund.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class RefundItemRequest {

    @NotNull
    private Long orderItemId;

    @NotNull
    @Min(1)
    private Integer quantity;
}
