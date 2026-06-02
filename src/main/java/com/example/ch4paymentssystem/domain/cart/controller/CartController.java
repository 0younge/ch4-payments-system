package com.example.ch4paymentssystem.domain.cart.controller;

import com.example.ch4paymentssystem.domain.cart.dto.AddCartItemRequest;
import com.example.ch4paymentssystem.domain.cart.dto.CartResponse;
import com.example.ch4paymentssystem.domain.cart.service.CartService;
import com.example.ch4paymentssystem.global.response.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class CartController {

    private final CartService cartService;

    // 장바구니 조회
    @GetMapping("/api/cart")
    public ResponseEntity<ApiResponse<CartResponse>> getCart() {
        CartResponse response = cartService.getCart(1L);
        return ResponseEntity.ok(ApiResponse.success("장바구니 조회에 성공했습니다.", response));
    }

    // 장바구니 담기
    @PostMapping("/api/cart/items")
    public ResponseEntity<ApiResponse<Void>> addCartItem(@Valid @RequestBody AddCartItemRequest request) {
        cartService.addCartItem(1L, request);
        return ResponseEntity.ok(ApiResponse.success("장바구니에 상품을 담았습니다."));
    }
}