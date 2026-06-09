package com.example.ch4paymentssystem.infra.portone.webhook;

import com.example.ch4paymentssystem.global.exception.BusinessException;
import com.example.ch4paymentssystem.global.exception.ErrorCode;
import com.example.ch4paymentssystem.infra.portone.config.PortOneProperties;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.Base64;

@Component
@RequiredArgsConstructor
public class PortOneWebhookVerifier {

    private static final String HMAC_SHA256 = "HmacSHA256";

    private final PortOneProperties portOneProperties;

    public void verify(HttpHeaders headers, String body) {
        String webhookSecret = portOneProperties.getWebhookSecret();
        if (!StringUtils.hasText(webhookSecret)) {
            return;
        }

        String webhookId = headers.getFirst("webhook-id");
        String webhookTimestamp = headers.getFirst("webhook-timestamp");
        String webhookSignature = headers.getFirst("webhook-signature");

        if (!StringUtils.hasText(webhookId)
                || !StringUtils.hasText(webhookTimestamp)
                || !StringUtils.hasText(webhookSignature)
                || body == null) {
            throw new BusinessException(ErrorCode.INVALID_WEBHOOK_REQUEST);
        }

        String signedContent = webhookId + "." + webhookTimestamp + "." + body;
        String expectedSignature = createSignature(webhookSecret, signedContent);
        if (!matches(webhookSignature, expectedSignature)) {
            throw new BusinessException(ErrorCode.INVALID_WEBHOOK_SIGNATURE);
        }
    }

    private String createSignature(String webhookSecret, String signedContent) {
        try {
            Mac mac = Mac.getInstance(HMAC_SHA256);
            mac.init(new SecretKeySpec(getSecretBytes(webhookSecret), HMAC_SHA256));
            byte[] signature = mac.doFinal(signedContent.getBytes(StandardCharsets.UTF_8));
            return Base64.getEncoder().encodeToString(signature);
        } catch (Exception e) {
            throw new BusinessException(ErrorCode.INVALID_WEBHOOK_SIGNATURE);
        }
    }

    private byte[] getSecretBytes(String webhookSecret) {
        String secret = webhookSecret.trim();
        if (secret.startsWith("whsec_")) {
            try {
                return Base64.getDecoder().decode(secret.substring("whsec_".length()));
            } catch (IllegalArgumentException e) {
                return secret.getBytes(StandardCharsets.UTF_8);
            }
        }

        return secret.getBytes(StandardCharsets.UTF_8);
    }

    private boolean matches(String signatureHeader, String expectedSignature) {
        String normalizedSignature = signatureHeader.replace("\"", "").trim();
        if (constantEquals(normalizedSignature, expectedSignature)) {
            return true;
        }

        String[] parts = normalizedSignature.split(",");
        for (int index = 0; index < parts.length; index++) {
            String part = parts[index].trim();
            if (constantEquals(part, expectedSignature)) {
                return true;
            }

            if (part.startsWith("v1=") && constantEquals(part.substring("v1=".length()), expectedSignature)) {
                return true;
            }

            if ("v1".equals(part) && index + 1 < parts.length) {
                String nextPart = parts[index + 1].trim();
                if (constantEquals(nextPart, expectedSignature)) {
                    return true;
                }
            }
        }

        return false;
    }

    private boolean constantEquals(String actual, String expected) {
        return MessageDigest.isEqual(
                actual.getBytes(StandardCharsets.UTF_8),
                expected.getBytes(StandardCharsets.UTF_8)
        );
    }
}
