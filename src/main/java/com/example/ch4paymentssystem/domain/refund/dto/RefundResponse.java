package com.example.ch4paymentssystem.domain.refund.dto;

import com.example.ch4paymentssystem.domain.payment.entity.Payment;
import com.example.ch4paymentssystem.domain.refund.entity.Refund;
import com.example.ch4paymentssystem.domain.refund.entity.RefundItem;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;

@Getter
@AllArgsConstructor
public class RefundResponse {

    private Long refundId;
    private Long orderId;
    private Long paymentId;
    private String refundStatus;
    private int totalRefundAmount;
    private int pointRefundAmount;
    private int pgRefundAmount;
    private String refundReason;
    private List<RefundItemResponse> refundItems;

    public static RefundResponse from(Refund refund, List<RefundItem> refundItems) {
        Payment payment = refund.getPayment();

        return new RefundResponse(
                refund.getId(),
                payment.getOrder().getId(),
                payment.getId(),
                refund.getRefundStatus().name(),
                refund.getTotalRefundAmount(),
                refund.getPointRefundAmount(),
                refund.getPgRefundAmount(),
                refund.getRefundReason(),
                refundItems.stream()
                        .map(RefundItemResponse::from)
                        .toList()
        );
    }
}
