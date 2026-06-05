package com.example.ch4paymentssystem.domain.order.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@AllArgsConstructor
public class OrderPaymentResponse {

    private Long paymentId;
    private String portonePaymentId;
    private String paymentStatus;
    private LocalDateTime paidAt;
}
