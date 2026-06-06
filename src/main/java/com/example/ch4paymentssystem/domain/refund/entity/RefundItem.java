package com.example.ch4paymentssystem.domain.refund.entity;

import com.example.ch4paymentssystem.common.BaseCreatedEntity;
import com.example.ch4paymentssystem.domain.order.entity.OrderItem;
import com.example.ch4paymentssystem.global.exception.BusinessException;
import com.example.ch4paymentssystem.global.exception.ErrorCode;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@Table(name = "refund_items")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class RefundItem extends BaseCreatedEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "refund_id", nullable = false)
    private Refund refund;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_item_id", nullable = false)
    private OrderItem orderItem;

    @Column(nullable = false)
    private Integer quantity;

    @Column(nullable = false)
    private Integer refundAmount;

    public static RefundItem create(
            Refund refund,
            OrderItem orderItem,
            int quantity,
            int refundAmount
    ) {
        if (refund == null || orderItem == null || refundAmount < 0) {
            throw new BusinessException(ErrorCode.INVALID_REFUND_REQUEST);
        }

        if (quantity <= 0) {
            throw new BusinessException(ErrorCode.INVALID_REFUND_QUANTITY);
        }

        RefundItem refundItem = new RefundItem();
        refundItem.refund = refund;
        refundItem.orderItem = orderItem;
        refundItem.quantity = quantity;
        refundItem.refundAmount = refundAmount;
        return refundItem;
    }
}
