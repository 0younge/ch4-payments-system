package com.example.ch4paymentssystem.domain.order.controller;

import com.example.ch4paymentssystem.domain.order.dto.request.CancelOrderRequest;
import com.example.ch4paymentssystem.domain.order.dto.request.CreateOrderRequest;
import com.example.ch4paymentssystem.domain.order.dto.response.CancelOrderResponse;
import com.example.ch4paymentssystem.domain.order.dto.response.CreateOrderResponse;
import com.example.ch4paymentssystem.domain.order.dto.response.OrderDetailResponse;
import com.example.ch4paymentssystem.domain.order.dto.response.OrderListResponse;
import com.example.ch4paymentssystem.domain.order.entity.OrderStatus;
import com.example.ch4paymentssystem.domain.order.service.OrderService;
import com.example.ch4paymentssystem.global.response.ApiResponse;
import com.example.ch4paymentssystem.global.security.AuthUser;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;

    @PostMapping
    public ResponseEntity<ApiResponse<CreateOrderResponse>> createOrder(
            @AuthenticationPrincipal Long userId,
            @RequestBody CreateOrderRequest request
    ) {
        CreateOrderResponse response = orderService.createOrder(userId, request);

        return ResponseEntity.ok(
                ApiResponse.success("주문과 결제 정보가 생성되었습니다.", response)
        );
    }

    @GetMapping("/{orderId}")
    public ResponseEntity<ApiResponse<OrderDetailResponse>> getOrderDetail(
            @AuthenticationPrincipal Long userId,
            @PathVariable Long orderId
    ) {
        OrderDetailResponse response = orderService.getOrderDetail(userId, orderId);

        return ResponseEntity.ok(
                ApiResponse.success("주문 상세 조회에 성공했습니다.", response)
        );
    }

    @GetMapping("/me")
    public ResponseEntity<ApiResponse<OrderListResponse>> getMyOrders(
            @AuthenticationPrincipal Long userId,
            @RequestParam(required = false) OrderStatus status,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        OrderListResponse response = orderService.getMyOrders(userId, status, page, size);

        return ResponseEntity.ok(
                ApiResponse.success("내 주문 내역 조회에 성공했습니다.", response)
        );
    }

    @PostMapping("/{orderId}/cancel")
    public ResponseEntity<ApiResponse<CancelOrderResponse>> cancelOrder(
            @AuthenticationPrincipal Long userId,
            @PathVariable Long orderId,
            @RequestBody CancelOrderRequest request
    ) {
        CancelOrderResponse response = orderService.cancelOrder(userId, orderId, request);

        return ResponseEntity.ok(
                ApiResponse.success("주문이 취소되었습니다.", response)
        );
    }

}
