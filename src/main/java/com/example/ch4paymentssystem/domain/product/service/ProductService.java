package com.example.ch4paymentssystem.domain.product.service;

import com.example.ch4paymentssystem.domain.product.dto.ProductResponseDto;
import com.example.ch4paymentssystem.domain.product.entity.Product;
import com.example.ch4paymentssystem.domain.product.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ProductService {

    private final ProductRepository productRepository;

    public ProductResponseDto getProduct(Long productId) {
        Product product = productRepository.findById(productId)
                .orElseThrow(
                        () -> new IllegalArgumentException("상품을 찾을 수 없습니다."));
        return new ProductResponseDto(product);
    }

}
