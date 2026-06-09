package com.example.ch4paymentssystem.domain.payment.controller;

import com.example.ch4paymentssystem.domain.payment.dto.PaymentConfirmRequest;
import com.example.ch4paymentssystem.domain.payment.dto.PaymentConfirmResponse;
import com.example.ch4paymentssystem.domain.payment.dto.PaymentConfigResponse;
import com.example.ch4paymentssystem.domain.payment.service.PaymentService;
import com.example.ch4paymentssystem.global.response.ApiResponse;
import com.example.ch4paymentssystem.infra.portone.config.PortOneProperties;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/payments")
@RequiredArgsConstructor
public class PaymentController {

    private final PaymentService paymentService;
    private final PortOneProperties portOneProperties;

    @GetMapping("/config")
    public ResponseEntity<ApiResponse<PaymentConfigResponse>> getPaymentConfig() {
        PaymentConfigResponse response = new PaymentConfigResponse(
                portOneProperties.getStoreId(),
                portOneProperties.getChannelKey()
        );

        return ResponseEntity.ok(
                ApiResponse.success("결제 설정을 조회했습니다.", response)
        );
    }

    @PostMapping("/confirm")
    public ResponseEntity<ApiResponse<PaymentConfirmResponse>> confirmPayment(
            @AuthenticationPrincipal Long userId,
            @Valid @RequestBody PaymentConfirmRequest request
    ) {
        PaymentConfirmResponse response = paymentService.confirmPayment(userId, request);

        return ResponseEntity.ok(
                ApiResponse.success("결제가 확정되었습니다.", response)
        );
    }
}
