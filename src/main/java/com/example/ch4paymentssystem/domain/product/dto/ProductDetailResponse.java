package com.example.ch4paymentssystem.domain.product.dto;

import com.example.ch4paymentssystem.domain.product.entity.Product;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor // 필드 다 받는 생성자 자동 생성
public class ProductDetailResponse {

    private Long id;
    private String name;
    private String description;
    private int price;
    private int stock;
    private String category;
    private String status;

    // 엔티티 -> DTO 변환 (팀 규칙대로 from() 씀)
    public static ProductDetailResponse from(Product product) {
        return new ProductDetailResponse(
                product.getId(),
                product.getName(),
                product.getDescription(),
                product.getPrice(),
                product.getStock(),
                product.getCategory(),
                product.getStatus().name() // 이넘 -> 문자열로
        );
    }
}
