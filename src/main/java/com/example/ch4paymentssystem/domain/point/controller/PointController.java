package com.example.ch4paymentssystem.domain.point.controller;

import com.example.ch4paymentssystem.domain.point.dto.PointBalanceResponse;
import com.example.ch4paymentssystem.domain.point.dto.PointHistoryPageResponse;
import com.example.ch4paymentssystem.domain.point.service.PointService;
import com.example.ch4paymentssystem.global.response.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/points")
public class PointController {

    private final PointService pointService;

    // 포인트 잔액 조회
    @GetMapping("/balance")
    public ResponseEntity<ApiResponse<PointBalanceResponse>> getPointBalance(
            @AuthenticationPrincipal Long userId
    ) {
        PointBalanceResponse response = pointService.getPointBalance(userId);
        return ResponseEntity.ok(ApiResponse.success("포인트 잔액 조회 성공", response));
    }

    // 포인트 거래 내역 조회
    @GetMapping("/histories")
    public ResponseEntity<ApiResponse<PointHistoryPageResponse>> getPointHistories(
            @AuthenticationPrincipal Long userId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        PointHistoryPageResponse response = pointService.getPointHistories(userId, page, size);
        return ResponseEntity.ok(ApiResponse.success("포인트 거래 내역 조회 성공", response));
    }
}
