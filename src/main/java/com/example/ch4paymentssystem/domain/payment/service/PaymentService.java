package com.example.ch4paymentssystem.domain.payment.service;

import com.example.ch4paymentssystem.domain.order.entity.Order;
import com.example.ch4paymentssystem.domain.payment.dto.PaymentConfirmRequest;
import com.example.ch4paymentssystem.domain.payment.dto.PaymentConfirmResponse;
import com.example.ch4paymentssystem.domain.payment.entity.Payment;
import com.example.ch4paymentssystem.domain.payment.entity.PaymentStatus;
import com.example.ch4paymentssystem.domain.payment.port.PaymentGateway;
import com.example.ch4paymentssystem.domain.payment.port.PaymentGatewayResponse;
import com.example.ch4paymentssystem.domain.payment.repository.PaymentRepository;
import com.example.ch4paymentssystem.global.exception.BusinessException;
import com.example.ch4paymentssystem.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class PaymentService {

    private static final String PORTONE_PAID_STATUS = "PAID";

    private final PaymentRepository paymentRepository;
    private final PaymentGateway paymentGateway;
    private final PaymentCommandService paymentCommandService;

    public PaymentConfirmResponse confirmPayment(Long userId, PaymentConfirmRequest request) {
        Payment payment = paymentRepository.findByOrderIdWithOrderAndUser(request.getOrderId())
                .orElseThrow(() -> new BusinessException(ErrorCode.PAYMENT_NOT_FOUND));
        Order order = payment.getOrder();

        if (!order.getUser().getId().equals(userId)) {
            throw new BusinessException(ErrorCode.FORBIDDEN_ORDER);
        }

        if (!payment.getPortonePaymentId().equals(request.getPortonePaymentId())) {
            throw new BusinessException(ErrorCode.INVALID_PAYMENT_REQUEST);
        }

        if (payment.getPaymentStatus() != PaymentStatus.READY) {
            return PaymentConfirmResponse.from(payment);
        }

        PaymentGatewayResponse portonePayment = paymentGateway.getPayment(payment.getPortonePaymentId());

        if (!payment.getPortonePaymentId().equals(portonePayment.id())) {
            cancelPortonePayment(payment.getPortonePaymentId(), "결제 식별자 불일치 자동 취소");
            paymentCommandService.failPaymentAndOrder(payment.getId(), "결제 식별자 불일치");
            throw new BusinessException(ErrorCode.INVALID_PAYMENT_REQUEST);
        }

        if (!PORTONE_PAID_STATUS.equals(portonePayment.status())) {
            paymentCommandService.failPaymentAndOrder(payment.getId(), "PortOne 결제 미완료");
            throw new BusinessException(ErrorCode.PAYMENT_NOT_PAID);
        }

        if (payment.getPgAmount() != portonePayment.totalAmount()) {
            cancelPortonePayment(payment.getPortonePaymentId(), "결제 금액 불일치 자동 취소");
            paymentCommandService.failPaymentAndOrder(payment.getId(), "결제 금액 불일치");
            throw new BusinessException(ErrorCode.PAYMENT_AMOUNT_MISMATCH);
        }

        try {
            return paymentCommandService.completePayment(payment.getId());
        } catch (BusinessException e) {
            if (e.getErrorCode() == ErrorCode.NOT_ENOUGH_POINT) {
                cancelPortonePayment(payment.getPortonePaymentId(), "포인트 잔액 부족 자동 취소");
                paymentCommandService.failPaymentAndOrder(payment.getId(), "포인트 잔액 부족");
            }
            throw e;
        }
    }

    private void cancelPortonePayment(String portonePaymentId, String reason) {
        try {
            paymentGateway.cancelPayment(portonePaymentId, reason);
        } catch (RuntimeException e) {
            log.warn("PortOne compensation cancel failed.", e);
        }
    }

}
