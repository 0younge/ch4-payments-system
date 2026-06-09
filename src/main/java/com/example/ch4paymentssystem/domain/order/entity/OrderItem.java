package com.example.ch4paymentssystem.domain.order.entity;

import com.example.ch4paymentssystem.common.BaseCreatedEntity;
import com.example.ch4paymentssystem.domain.product.entity.Product;
import com.example.ch4paymentssystem.global.exception.BusinessException;
import com.example.ch4paymentssystem.global.exception.ErrorCode;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.ColumnDefault;

@Getter
@Entity
@Table(name = "order_items")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class OrderItem extends BaseCreatedEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id", nullable = false)
    private Order order;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    @Column(nullable = false, length = 100)
    private String productName;

    @Column(nullable = false)
    private Integer productPrice;

    @Column(nullable = false)
    private Integer quantity;

    @Column(nullable = false)
    @ColumnDefault("0")
    private Integer refundedQuantity = 0;

    @Column(nullable = false)
    private Integer totalAmount;

    public static OrderItem create(
            Order order,
            Product product,
            String productName,
            int productPrice,
            int quantity
    ) {
        if (productPrice < 0) {
            throw new BusinessException(ErrorCode.INVALID_ORDER_AMOUNT);
        }

        if (quantity <= 0) {
            throw new BusinessException(ErrorCode.INVALID_QUANTITY);
        }

        OrderItem orderItem = new OrderItem();
        orderItem.order = order;
        orderItem.product = product;
        orderItem.productName = productName;
        orderItem.productPrice = productPrice;
        orderItem.quantity = quantity;
        orderItem.totalAmount = productPrice * quantity;
        return orderItem;
    }

    public int getRefundableQuantity() {
        return this.quantity - this.refundedQuantity;
    }

    public int calculateRefundAmount(int refundQuantity) {
        validateRefundQuantity(refundQuantity);

        long refundAmount = (long) this.productPrice * refundQuantity;
        if (refundAmount > Integer.MAX_VALUE) {
            throw new BusinessException(ErrorCode.INVALID_REFUND_REQUEST);
        }

        return (int) refundAmount;
    }

    public void refund(int refundQuantity) {
        validateRefundQuantity(refundQuantity);

        this.product.increaseStock(refundQuantity);
        this.refundedQuantity += refundQuantity;
    }

    private void validateRefundQuantity(int refundQuantity) {
        if (refundQuantity <= 0) {
            throw new BusinessException(ErrorCode.INVALID_REFUND_QUANTITY);
        }

        if (refundQuantity > getRefundableQuantity()) {
            throw new BusinessException(ErrorCode.INVALID_REFUND_QUANTITY);
        }
    }
}
