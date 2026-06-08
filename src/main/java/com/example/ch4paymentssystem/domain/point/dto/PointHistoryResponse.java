package com.example.ch4paymentssystem.domain.point.dto;


import com.example.ch4paymentssystem.domain.point.entity.PointHistory;
import com.example.ch4paymentssystem.domain.point.entity.PointType;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@AllArgsConstructor
public class PointHistoryResponse {

    private Long pointHistoryId;     // 거래 내역 ID
    private PointType pointType;     // 거래타입 (USE, EARN, REFUND_USED,CANCEL_EARNED)
    private Integer amount;          // 변동 포인트
    private Integer balanceAfter;    // 변동 후 잔액
    private String description;      // 설명
    private LocalDateTime createdAt; // 처리 일시

    public static PointHistoryResponse from(PointHistory pointHistory) {
        return new PointHistoryResponse(
                pointHistory.getId(),
                pointHistory.getPointType(),
                pointHistory.getAmount(),
                pointHistory.getBalanceAfter(),
                pointHistory.getDescription(),
                pointHistory.getCreatedAt()
        );
    }
}
