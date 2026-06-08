package com.example.ch4paymentssystem.domain.refund.service;

import com.example.ch4paymentssystem.domain.payment.port.PaymentGateway;
import com.example.ch4paymentssystem.domain.refund.dto.RefundRequest;
import com.example.ch4paymentssystem.domain.refund.dto.RefundResponse;
import com.example.ch4paymentssystem.domain.refund.entity.RefundStatus;
import com.example.ch4paymentssystem.global.exception.BusinessException;
import com.example.ch4paymentssystem.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class RefundService {

    private final RefundCommandService refundCommandService;
    private final PaymentGateway paymentGateway;

    public RefundResponse requestRefund(Long userId, RefundRequest request) {
        RefundResponse requestedRefund = refundCommandService.createRefundRequest(userId, request);

        if (RefundStatus.COMPLETED.name().equals(requestedRefund.getRefundStatus())) {
            return requestedRefund;
        }

        try {
            cancelPgPayment(requestedRefund);
        } catch (RuntimeException e) {
            refundCommandService.failRefund(requestedRefund.getRefundId(), "PortOne 환불 실패");
            throw new BusinessException(ErrorCode.REFUND_FAILED);
        }

        return refundCommandService.completeRefund(requestedRefund.getRefundId());
    }

    private void cancelPgPayment(RefundResponse refund) {
        if (refund.getPgRefundAmount() == 0) {
            return;
        }

        paymentGateway.cancelPayment(
                refund.getPortonePaymentId(),
                refund.getPgRefundAmount(),
                refund.getRefundReason(),
                "refund-" + refund.getRefundId()
        );
    }
}
