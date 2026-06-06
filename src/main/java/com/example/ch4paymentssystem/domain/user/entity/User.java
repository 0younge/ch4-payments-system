package com.example.ch4paymentssystem.domain.user.entity;

import com.example.ch4paymentssystem.common.BaseTimeEntity;
import com.example.ch4paymentssystem.global.exception.BusinessException;
import com.example.ch4paymentssystem.global.exception.ErrorCode;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.ColumnDefault;

@Getter
@Entity
@Table(name = "users")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class User extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(nullable = false)
    private String password;

    @Column(nullable = false, length = 50)
    private String name;

    @Column(nullable = false, length = 30)
    private String phoneNumber;

    @Column(nullable = false)
    @ColumnDefault("0")
    private Integer pointBalance = 0;

    public User(String email, String password, String name, String phoneNumber) {
        this.email = email;
        this.password = password;
        this.name = name;
        this.phoneNumber = phoneNumber;
        this.pointBalance = 0;
    }

    public void usePoint(int amount) {
        if (amount < 0) {
            throw new BusinessException(ErrorCode.INVALID_POINT_AMOUNT);
        }

        if (this.pointBalance < amount) {
            throw new BusinessException(ErrorCode.NOT_ENOUGH_POINT);
        }

        this.pointBalance -= amount;
    }

    public void earnPoint(int amount) {
        if (amount < 0) {
            throw new BusinessException(ErrorCode.INVALID_POINT_AMOUNT);
        }

        this.pointBalance += amount;
    }

}
