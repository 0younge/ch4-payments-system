package com.example.ch4paymentssystem.domain.refund.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@NoArgsConstructor
public class RefundRequest {

    @NotNull
    private Long orderId;

    @NotBlank
    private String refundReason;

    @Valid
    @NotEmpty
    private List<RefundItemRequest> refundItems;
}
