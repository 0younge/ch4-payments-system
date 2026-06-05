package com.example.ch4paymentssystem.domain.cart.controller;

import com.example.ch4paymentssystem.domain.cart.dto.AddCartItemRequest;
import com.example.ch4paymentssystem.domain.cart.dto.CartResponse;
import com.example.ch4paymentssystem.domain.cart.dto.UpdateCartItemQuantityRequest;
import com.example.ch4paymentssystem.domain.cart.service.CartService;
import com.example.ch4paymentssystem.global.response.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

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

    // 장바구니 수량 변경
    @PatchMapping("/api/cart/items/{cartItemId}")
    public ResponseEntity<ApiResponse<Void>> updateCartItemQuantity(@PathVariable Long cartItemId, @Valid @RequestBody UpdateCartItemQuantityRequest request) {
        cartService.updateCartItemQuantity(1L, cartItemId, request);
        return ResponseEntity.ok(ApiResponse.success("수량이 변경되었습니다."));
    }

    // 장바구니 상품 개별 삭제
    @DeleteMapping("/api/cart/items/{cartItemId}")
    public ResponseEntity<ApiResponse<Void>> deleteCartItem(@PathVariable Long cartItemId) {
        cartService.deleteCartItem(1L, cartItemId);
        return ResponseEntity.ok(ApiResponse.success("장바구니 상품이 삭제되었습니다."));
    }
}