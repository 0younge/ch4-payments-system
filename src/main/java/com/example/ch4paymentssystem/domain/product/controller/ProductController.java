package com.example.ch4paymentssystem.domain.product.controller;

import com.example.ch4paymentssystem.domain.product.dto.ProductDetailResponse;
import com.example.ch4paymentssystem.domain.product.service.ProductService;
import com.example.ch4paymentssystem.global.response.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/products")
public class ProductController {

    private final ProductService productService;

    @GetMapping("/{productId}")
    public ResponseEntity<ApiResponse<ProductDetailResponse>> getProduct(@PathVariable Long productId) {
        // ApiResponse로 감싸서 반환 (팀 공통 응답 형식)
        ProductDetailResponse response = productService.getProduct(productId);
        return ResponseEntity.ok(ApiResponse.success("상품 조회 성공", response));
    }
}
