package com.example.ch4paymentssystem.domain.cart.controller;

import com.example.ch4paymentssystem.domain.cart.service.CartService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class CartController {

    private final CartService cartService;

}
