package com.example.ch4paymentssystem.domain.payment.controller;

import com.example.ch4paymentssystem.domain.payment.service.PaymentService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class PaymentController {

    private final PaymentService paymentService;

}
