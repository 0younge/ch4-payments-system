package com.example.ch4paymentssystem.domain.payment.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class PaymentConfirmRequest {

    @NotNull
    private Long orderId;

    @NotBlank
    private String portonePaymentId;
}
