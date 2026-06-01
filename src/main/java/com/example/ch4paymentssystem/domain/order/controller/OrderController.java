package com.example.ch4paymentssystem.domain.order.controller;

import com.example.ch4paymentssystem.domain.order.service.OrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;

}
