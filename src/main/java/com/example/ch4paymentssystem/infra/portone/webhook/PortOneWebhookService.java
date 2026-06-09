package com.example.ch4paymentssystem.infra.portone.webhook;

import com.example.ch4paymentssystem.domain.payment.service.PaymentService;
import com.example.ch4paymentssystem.global.exception.BusinessException;
import com.example.ch4paymentssystem.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import tools.jackson.databind.ObjectMapper;

import java.util.Collection;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class PortOneWebhookService {

    private final PortOneWebhookVerifier portOneWebhookVerifier;
    private final PaymentService paymentService;
    private final ObjectMapper objectMapper;

    public void handle(HttpHeaders headers, String body) {
        portOneWebhookVerifier.verify(headers, body);

        PortOneWebhookPayload payload = parse(body);
        if (!payload.isPaymentPaidEvent()) {
            return;
        }

        paymentService.confirmPaymentByWebhook(payload.portonePaymentId());
    }

    private PortOneWebhookPayload parse(String body) {
        try {
            Object parsedBody = objectMapper.readValue(body, Map.class);
            if (!(parsedBody instanceof Map<?, ?> payload)) {
                throw new BusinessException(ErrorCode.INVALID_WEBHOOK_REQUEST);
            }

            String eventType = findDirectText(payload, "type", "eventType");
            if (StringUtils.hasText(eventType) && !eventType.endsWith(".Paid")) {
                return new PortOneWebhookPayload(eventType, null);
            }

            String portonePaymentId = findText(payload, "paymentId", "payment_id", "portonePaymentId");
            if (!StringUtils.hasText(portonePaymentId)) {
                throw new BusinessException(ErrorCode.INVALID_WEBHOOK_REQUEST);
            }

            return new PortOneWebhookPayload(eventType, portonePaymentId);
        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            throw new BusinessException(ErrorCode.INVALID_WEBHOOK_REQUEST);
        }
    }

    private String findDirectText(Map<?, ?> payload, String... fieldNames) {
        for (String fieldName : fieldNames) {
            Object fieldValue = payload.get(fieldName);
            if (fieldValue instanceof String textValue && StringUtils.hasText(textValue)) {
                return textValue;
            }
        }

        return null;
    }

    private String findText(Object value, String... fieldNames) {
        if (value instanceof Map<?, ?> map) {
            for (String fieldName : fieldNames) {
                Object fieldValue = map.get(fieldName);
                if (fieldValue instanceof String textValue && StringUtils.hasText(textValue)) {
                    return textValue;
                }
            }

            for (Object childValue : map.values()) {
                String textValue = findText(childValue, fieldNames);
                if (StringUtils.hasText(textValue)) {
                    return textValue;
                }
            }
        }

        if (value instanceof Collection<?> collection) {
            for (Object childValue : collection) {
                String textValue = findText(childValue, fieldNames);
                if (StringUtils.hasText(textValue)) {
                    return textValue;
                }
            }
        }

        return null;
    }

    private record PortOneWebhookPayload(
            String eventType,
            String portonePaymentId
    ) {

        private boolean isPaymentPaidEvent() {
            return !StringUtils.hasText(eventType) || eventType.endsWith(".Paid");
        }
    }
}
