package com.example.ch4paymentssystem.domain.product.service;

import com.example.ch4paymentssystem.domain.product.dto.ProductDetailResponse;
import com.example.ch4paymentssystem.domain.product.dto.ProductListResponse;
import com.example.ch4paymentssystem.domain.product.entity.Product;
import com.example.ch4paymentssystem.domain.product.entity.ProductSort;
import com.example.ch4paymentssystem.domain.product.entity.ProductStatus;
import com.example.ch4paymentssystem.domain.product.repository.ProductRepository;
import com.example.ch4paymentssystem.global.exception.BusinessException;
import com.example.ch4paymentssystem.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ProductService {

    private final ProductRepository productRepository;

    @Transactional(readOnly = true) // 조회니까 readOnly (팀 규칙)
    public Page<ProductListResponse> getProducts(
            String category, Integer minPrice, Integer maxPrice,
            ProductStatus status, ProductSort sort, int page, int size) {

        // 정렬 기준에 따라 Sort 방식 결정
        Sort sortCondition;
        if (sort == ProductSort.PRICE_ASC) {
            sortCondition = Sort.by("price").ascending();   // 가격 낮은 순
        } else if (sort == ProductSort.PRICE_DESC) {
            sortCondition = Sort.by("price").descending();  // 가격 높은 순
        } else {
            sortCondition = Sort.by("createdAt").descending(); // 최신순 (기본값)
        }

        // 페이지 번호, 크기, 정렬 조건 묶어서 pageable 만들기
        Pageable pageable = PageRequest.of(page, size, sortCondition);

        // DB에서 필터 조건으로 조회하고 DTO로 변환해서 반환
        return productRepository.findAllWithFilters(category, minPrice, maxPrice, status, pageable)
                .map(ProductListResponse::from);
    }

    @Transactional(readOnly = true) // 조회니까 readOnly (팀 규칙)
    public ProductDetailResponse getProduct(Long productId) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new BusinessException(ErrorCode.PRODUCT_NOT_FOUND));
        return ProductDetailResponse.from(product); // from()으로 변환
    }

}
