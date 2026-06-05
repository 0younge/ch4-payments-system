package com.example.ch4paymentssystem.domain.order.dto.request;

import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@NoArgsConstructor
public class OrderPreviewRequest {

    private List<Long> cartItemIds;
}