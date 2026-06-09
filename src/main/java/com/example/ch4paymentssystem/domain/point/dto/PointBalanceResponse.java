package com.example.ch4paymentssystem.domain.point.dto;


import com.example.ch4paymentssystem.domain.user.entity.User;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class PointBalanceResponse {

    private Integer pointBalance; // 현재 포인트 잔액

    public static PointBalanceResponse from(User user) {
        return new PointBalanceResponse(user.getPointBalance());
    }
}
