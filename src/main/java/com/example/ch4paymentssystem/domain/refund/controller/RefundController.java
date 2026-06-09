package com.example.ch4paymentssystem.domain.refund.controller;

import com.example.ch4paymentssystem.domain.refund.dto.RefundRequest;
import com.example.ch4paymentssystem.domain.refund.dto.RefundResponse;
import com.example.ch4paymentssystem.domain.refund.service.RefundService;
import com.example.ch4paymentssystem.global.response.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/refunds")
@RequiredArgsConstructor
public class RefundController {

    private final RefundService refundService;

    @PostMapping
    public ResponseEntity<ApiResponse<RefundResponse>> requestRefund(
            @AuthenticationPrincipal Long userId,
            @Valid @RequestBody RefundRequest request
    ) {
        RefundResponse response = refundService.requestRefund(userId, request);

        return ResponseEntity.ok(
                ApiResponse.success("환불이 완료되었습니다.", response)
        );
    }
}
