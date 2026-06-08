package com.example.ch4paymentssystem.domain.cart.entity;

import com.example.ch4paymentssystem.common.BaseTimeEntity;
import com.example.ch4paymentssystem.domain.product.entity.Product;
import com.example.ch4paymentssystem.global.exception.BusinessException;
import com.example.ch4paymentssystem.global.exception.ErrorCode;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@Table(
        name = "cart_items",
        uniqueConstraints = @UniqueConstraint(columnNames = {"cart_id", "product_id"})
)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class CartItem extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cart_id", nullable = false)
    private Cart cart;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    @Column(nullable = false)
    private Integer quantity;

    public CartItem(Cart cart, Product product, Integer quantity) {
        validateQuantity(quantity);

        this.cart = cart;
        this.product = product;
        this.quantity = quantity;
    }

    public void addQuantity(int quantity) {
        validateQuantity(quantity);

        long nextQuantity = (long) this.quantity + quantity;
        if (nextQuantity > Integer.MAX_VALUE) {
            throw new BusinessException(ErrorCode.INVALID_QUANTITY);
        }

        this.quantity = (int) nextQuantity;
    }

    public void updateQuantity(int quantity) {
        validateQuantity(quantity);

        this.quantity = quantity;
    }

    private void validateQuantity(Integer quantity) {
        if (quantity == null || quantity <= 0) {
            throw new BusinessException(ErrorCode.INVALID_QUANTITY);
        }
    }
}
