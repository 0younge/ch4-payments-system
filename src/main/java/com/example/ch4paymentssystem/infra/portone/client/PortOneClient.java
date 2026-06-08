package com.example.ch4paymentssystem.infra.portone.client;

import com.example.ch4paymentssystem.domain.payment.port.PaymentGateway;
import com.example.ch4paymentssystem.domain.payment.port.PaymentGatewayResponse;
import com.example.ch4paymentssystem.infra.portone.config.PortOneProperties;
import com.example.ch4paymentssystem.infra.portone.dto.PortOneCancelRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.util.Map;

@Component
@RequiredArgsConstructor
public class PortOneClient implements PaymentGateway {

    private final RestClient portOneRestClient;
    private final PortOneProperties portOneProperties;

    @Override
    public PaymentGatewayResponse getPayment(String portonePaymentId) {
        Map<?, ?> response = portOneRestClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/payments/{paymentId}")
                        .queryParam("storeId", portOneProperties.getStoreId())
                        .build(portonePaymentId))
                .retrieve()
                .body(Map.class);

        if (response == null) {
            throw new IllegalStateException("PortOne 결제 조회 응답이 올바르지 않습니다.");
        }

        String id = getString(response, "id");
        String status = getString(response, "status");
        int totalAmount = getTotalAmount(response);

        return new PaymentGatewayResponse(
                id,
                status,
                totalAmount
        );
    }

    @Override
    public void cancelPayment(String portonePaymentId, int amount, String reason, String idempotencyKey) {
        portOneRestClient.post()
                .uri("/payments/{paymentId}/cancel", portonePaymentId)
                .header("Idempotency-Key", idempotencyKey)
                .body(new PortOneCancelRequest(reason, amount, portOneProperties.getStoreId()))
                .retrieve()
                .toBodilessEntity();
    }

    private String getString(Map<?, ?> response, String key) {
        Object value = response.get(key);

        if (!(value instanceof String stringValue)) {
            throw new IllegalStateException("PortOne 결제 조회 응답이 올바르지 않습니다.");
        }

        return stringValue;
    }

    private int getTotalAmount(Map<?, ?> response) {
        Object amountValue = response.get("amount");

        if (!(amountValue instanceof Map<?, ?> amount)) {
            throw new IllegalStateException("PortOne 결제 조회 응답이 올바르지 않습니다.");
        }

        Object totalValue = amount.get("total");

        if (!(totalValue instanceof Number total)) {
            throw new IllegalStateException("PortOne 결제 조회 응답이 올바르지 않습니다.");
        }

        return total.intValue();
    }
}
