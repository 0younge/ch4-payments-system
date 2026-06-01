package com.example.ch4paymentssystem.domain.refund.controller;

import com.example.ch4paymentssystem.domain.refund.service.RefundService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class RefundController {

    private final RefundService refundService;

}
