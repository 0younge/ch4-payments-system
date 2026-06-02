package com.example.ch4paymentssystem.domain.product.dto;


import com.example.ch4paymentssystem.domain.product.entity.Product;
import lombok.Getter;

@Getter
public class ProductResponseDto {

    private final Long id;
    private final String name;
    private final String description;
    private final Integer price;
    private final Integer stock;
    private final String category;
    private final String status;

    public ProductResponseDto(Product product) {
        this.id = product.getId();
        this.name = product.getName();
        this.description = product.getDescription();
        this.price = product.getPrice();
        this.stock = product.getStock();
        this.category = product.getCategory();
        this.status = product.getStatus().name();
    }
}
