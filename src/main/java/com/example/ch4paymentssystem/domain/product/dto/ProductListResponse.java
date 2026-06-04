package com.example.ch4paymentssystem.domain.product.dto;

import com.example.ch4paymentssystem.domain.product.entity.Product;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor // 필드 전부 받는 생성자 자동 생성
public class ProductListResponse {

    private Long productId;    // 상품 ID
    private String name;      // 상품명
    private int price;        // 판매가
    private int stock;        // 재고 수량
    private String category;  // 카테고리
    private String status;    // 판매 상태

    // 엔티티 -> DTO 변환 (팀 규칙대로 from() 씀)
    public static ProductListResponse from(Product product) {
        return new ProductListResponse(
                product.getId(),   // productId
                product.getName(),
                product.getPrice(),
                product.getStock(),
                product.getCategory(),
                product.getStatus().name() // enum -> 문자열로 변환
        );
    }
}
