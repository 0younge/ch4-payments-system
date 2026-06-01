package com.example.ch4paymentssystem.domain.refund.service;

import com.example.ch4paymentssystem.domain.refund.repository.RefundItemRepository;
import com.example.ch4paymentssystem.domain.refund.repository.RefundRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class RefundService {

    private final RefundRepository refundRepository;
    private final RefundItemRepository refundItemRepository;

}
