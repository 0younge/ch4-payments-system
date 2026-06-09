package com.example.ch4paymentssystem.domain.product.controller;

import com.example.ch4paymentssystem.domain.product.dto.ProductDetailResponse;
import com.example.ch4paymentssystem.domain.product.dto.ProductListResponse;
import com.example.ch4paymentssystem.domain.product.dto.ProductPageResponse;
import com.example.ch4paymentssystem.domain.product.entity.ProductSort;
import com.example.ch4paymentssystem.domain.product.entity.ProductStatus;
import com.example.ch4paymentssystem.domain.product.service.ProductService;
import com.example.ch4paymentssystem.global.response.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/products")
public class ProductController {

    private final ProductService productService;

    // 상품 목록 조회 (필터, 정렬, 페이지네이션 다 됨)
    @GetMapping
    public ResponseEntity<ApiResponse<ProductPageResponse>> getProducts(
            @RequestParam(required = false) String category,         // 없으면 전체 카테고리
            @RequestParam(required = false) Integer minPrice,        // 없으면 최소가격 제한 없음
            @RequestParam(required = false) Integer maxPrice,        // 없으면 최대가격 제한 없음
            @RequestParam(required = false) ProductStatus status,    // 없으면 판매상태 제한 없음
            @RequestParam(defaultValue = "LATEST") ProductSort sort, // 기본값은 최신순
            @RequestParam(defaultValue = "0") int page,              // 페이지 번호 (0이 첫 페이지)
            @RequestParam(defaultValue = "20") int size              // 한 페이지에 보여줄 개수
    ) {
        Page<ProductListResponse> pageResult = productService.getProducts(
                category, minPrice, maxPrice, status, sort, page, size);
        // Page 객체를 팀 스펙에 맞는 응답 형태로 변환
        return ResponseEntity.ok(ApiResponse.success("상품 목록 조회 성공", ProductPageResponse.from(pageResult)));
    }

    // 상품 단건 조회
    @GetMapping("/{productId}")
    public ResponseEntity<ApiResponse<ProductDetailResponse>> getProduct(@PathVariable Long productId) {
        // ApiResponse로 감싸서 반환 (팀 공통 응답 형식)
        ProductDetailResponse response = productService.getProduct(productId);
        return ResponseEntity.ok(ApiResponse.success("상품 조회 성공", response));
    }
}
