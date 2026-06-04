package com.example.ch4paymentssystem.domain.product.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.data.domain.Page;

import java.util.List;

@Getter
@AllArgsConstructor // 필드 전부 받는 생성자 자동 생성
public class ProductPageResponse {

    private List<ProductListResponse> products; // 상품 목록
    private int page;                           // 현재 페이지 번호
    private int size;                           // 페이지 크기
    private long totalElements;                 // 전체 상품 수
    private int totalPages;                     // 전체 페이지 수

    // Page 객체를 우리가 원하는 응답 형태로 변환
    public static ProductPageResponse from(Page<ProductListResponse> pageData) {
        return new ProductPageResponse(
                pageData.getContent(),       // 상품 목록 (Spring은 content라 부르는 것)
                pageData.getNumber(),        // 현재 페이지 번호 (Spring은 number라 부르는 것)
                pageData.getSize(),          // 페이지 크기
                pageData.getTotalElements(), // 전체 상품 수
                pageData.getTotalPages()     // 전체 페이지 수
        );
    }
}
