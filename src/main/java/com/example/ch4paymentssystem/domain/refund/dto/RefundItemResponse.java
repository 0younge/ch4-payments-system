package com.example.ch4paymentssystem.domain.refund.dto;

import com.example.ch4paymentssystem.domain.refund.entity.RefundItem;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class RefundItemResponse {

    private Long orderItemId;
    private int quantity;
    private int refundAmount;

    public static RefundItemResponse from(RefundItem refundItem) {
        return new RefundItemResponse(
                refundItem.getOrderItem().getId(),
                refundItem.getQuantity(),
                refundItem.getRefundAmount()
        );
    }
}
