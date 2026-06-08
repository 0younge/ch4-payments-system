package com.example.ch4paymentssystem.domain.payment.controller;

import com.example.ch4paymentssystem.global.response.ApiResponse;
import com.example.ch4paymentssystem.infra.portone.webhook.PortOneWebhookService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/payments")
@RequiredArgsConstructor
public class PaymentWebhookController {

    private final PortOneWebhookService portOneWebhookService;

    @PostMapping("/webhook")
    public ResponseEntity<ApiResponse<Void>> handleWebhook(
            @RequestHeader HttpHeaders headers,
            @RequestBody String body
    ) {
        portOneWebhookService.handle(headers, body);

        return ResponseEntity.ok(
                ApiResponse.success("웹훅이 처리되었습니다.")
        );
    }
}
