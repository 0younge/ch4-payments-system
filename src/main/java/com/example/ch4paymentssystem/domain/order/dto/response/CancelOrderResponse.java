package com.example.ch4paymentssystem.domain.order.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@AllArgsConstructor
public class CancelOrderResponse {

    private Long orderId;
    private String orderNumber;
    private String orderStatus;
    private Long paymentId;
    private String paymentStatus;
    private List<RestoredStockResponse> restoredStock;
    private LocalDateTime canceledAt;
}