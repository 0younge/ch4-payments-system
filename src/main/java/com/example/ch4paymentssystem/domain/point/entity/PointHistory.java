package com.example.ch4paymentssystem.domain.point.entity;

import com.example.ch4paymentssystem.common.BaseCreatedEntity;
import com.example.ch4paymentssystem.domain.order.entity.Order;
import com.example.ch4paymentssystem.domain.user.entity.User;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@Table(name = "point_histories")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class PointHistory extends BaseCreatedEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id")
    private Order order;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private PointType pointType;

    @Column(nullable = false)
    private Integer amount;

    @Column(nullable = false)
    private Integer balanceAfter;

    @Column(nullable = false, length = 255)
    private String description;

    public static PointHistory create(
            User user,
            Order order,
            PointType pointType,
            int amount,
            int balanceAfter,
            String description
    ) {
        PointHistory pointHistory = new PointHistory();
        pointHistory.user = user;
        pointHistory.order = order;
        pointHistory.pointType = pointType;
        pointHistory.amount = amount;
        pointHistory.balanceAfter = balanceAfter;
        pointHistory.description = description;
        return pointHistory;
    }

}
