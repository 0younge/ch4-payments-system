package com.example.ch4paymentssystem.domain.product.controller;

import com.example.ch4paymentssystem.domain.product.service.ProductService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class ProductController {

    private final ProductService productService;

}
