package com.example.ch4paymentssystem.domain.order.entity;

import com.example.ch4paymentssystem.common.BaseTimeEntity;
import com.example.ch4paymentssystem.domain.user.entity.User;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.ColumnDefault;

import java.time.LocalDateTime;

@Getter
@Entity
@Table(name = "orders")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Order extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false, unique = true, length = 100)
    private String orderNumber;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private OrderStatus orderStatus;

    @Column(nullable = false)
    private Integer totalProductAmount;

    @Column(nullable = false)
    @ColumnDefault("0")
    private Integer usedPointAmount = 0;

    @Column(nullable = false)
    private Integer pgPaymentAmount;

    @Column(nullable = false)
    @ColumnDefault("0")
    private Integer earnedPointAmount = 0;

    @Column(nullable = false)
    private LocalDateTime orderedAt;

    private LocalDateTime cancelledAt;

    public static Order create(
            User user,
            String orderNumber,
            int totalProductAmount,
            int usedPointAmount,
            OrderStatus orderStatus
    ) {
        Order order = new Order();
        order.user = user;
        order.orderNumber = orderNumber;
        order.totalProductAmount = totalProductAmount;
        order.usedPointAmount = usedPointAmount;
        order.orderStatus = orderStatus;
        return order;
    }

}
