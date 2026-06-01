package com.example.ch4paymentssystem.domain.product.repository;

import com.example.ch4paymentssystem.domain.product.entity.Product;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProductRepository extends JpaRepository<Product, Long> {
}
