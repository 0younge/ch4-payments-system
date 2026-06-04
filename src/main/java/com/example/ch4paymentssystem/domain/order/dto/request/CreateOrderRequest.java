package com.example.ch4paymentssystem.domain.order.dto.request;

import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@NoArgsConstructor
public class CreateOrderRequest {

    private List<Long> cartItemIds;
    private int usedPointAmount;
}
