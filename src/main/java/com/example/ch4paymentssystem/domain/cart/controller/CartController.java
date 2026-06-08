package com.example.ch4paymentssystem.domain.cart.controller;

import com.example.ch4paymentssystem.domain.cart.dto.AddCartItemRequest;
import com.example.ch4paymentssystem.domain.cart.dto.CartResponse;
import com.example.ch4paymentssystem.domain.cart.dto.UpdateCartItemQuantityRequest;
import com.example.ch4paymentssystem.domain.cart.service.CartService;
import com.example.ch4paymentssystem.global.response.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/cart")
public class CartController {

    private final CartService cartService;

    @GetMapping
    public ResponseEntity<ApiResponse<CartResponse>> getCart(
            @AuthenticationPrincipal Long userId
    ) {
        CartResponse response = cartService.getCart(userId);
        return ResponseEntity.ok(ApiResponse.success("장바구니 조회에 성공했습니다.", response));
    }

    @PostMapping("/items")
    public ResponseEntity<ApiResponse<Void>> addCartItem(
            @AuthenticationPrincipal Long userId,
            @Valid @RequestBody AddCartItemRequest request
    ) {
        cartService.addCartItem(userId, request);
        return ResponseEntity.ok(ApiResponse.success("장바구니에 상품을 담았습니다."));
    }

    @PatchMapping("/items/{cartItemId}")
    public ResponseEntity<ApiResponse<Void>> updateCartItemQuantity(
            @AuthenticationPrincipal Long userId,
            @PathVariable Long cartItemId,
            @Valid @RequestBody UpdateCartItemQuantityRequest request
    ) {
        cartService.updateCartItemQuantity(userId, cartItemId, request);
        return ResponseEntity.ok(ApiResponse.success("수량이 변경되었습니다."));
    }

    @DeleteMapping("/items/{cartItemId}")
    public ResponseEntity<ApiResponse<Void>> deleteCartItem(
            @AuthenticationPrincipal Long userId,
            @PathVariable Long cartItemId
    ) {
        cartService.deleteCartItem(userId, cartItemId);
        return ResponseEntity.ok(ApiResponse.success("장바구니 상품이 삭제되었습니다."));
    }

    @DeleteMapping("/items")
    public ResponseEntity<ApiResponse<Void>> clearCart(
            @AuthenticationPrincipal Long userId
    ) {
        cartService.clearCart(userId);
        return ResponseEntity.ok(ApiResponse.success("장바구니를 비웠습니다."));
    }
}
