package com.example.ch4paymentssystem.domain.refund.service;

import com.example.ch4paymentssystem.domain.order.entity.Order;
import com.example.ch4paymentssystem.domain.order.entity.OrderItem;
import com.example.ch4paymentssystem.domain.order.repository.OrderItemRepository;
import com.example.ch4paymentssystem.domain.payment.entity.Payment;
import com.example.ch4paymentssystem.domain.payment.entity.PaymentStatus;
import com.example.ch4paymentssystem.domain.payment.repository.PaymentRepository;
import com.example.ch4paymentssystem.domain.refund.dto.RefundItemRequest;
import com.example.ch4paymentssystem.domain.refund.dto.RefundRequest;
import com.example.ch4paymentssystem.domain.refund.dto.RefundResponse;
import com.example.ch4paymentssystem.domain.refund.entity.Refund;
import com.example.ch4paymentssystem.domain.refund.entity.RefundItem;
import com.example.ch4paymentssystem.domain.refund.entity.RefundStatus;
import com.example.ch4paymentssystem.domain.refund.repository.RefundItemRepository;
import com.example.ch4paymentssystem.domain.refund.repository.RefundRepository;
import com.example.ch4paymentssystem.global.exception.BusinessException;
import com.example.ch4paymentssystem.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class RefundService {

    private final RefundRepository refundRepository;
    private final RefundItemRepository refundItemRepository;
    private final PaymentRepository paymentRepository;
    private final OrderItemRepository orderItemRepository;

    @Transactional
    public RefundResponse requestRefund(Long userId, RefundRequest request) {
        validateRequest(request);

        Payment payment = paymentRepository.findByOrderIdWithOrderAndUser(request.getOrderId())
                .orElseThrow(() -> new BusinessException(ErrorCode.PAYMENT_NOT_FOUND));
        Order order = payment.getOrder();

        validateOwner(userId, order);
        validatePaymentStatus(payment);

        Map<Long, Integer> requestedQuantities = getRequestedQuantities(request.getRefundItems());
        List<OrderItem> orderItems = orderItemRepository.findAllByOrderIdAndIdIn(
                order.getId(),
                requestedQuantities.keySet().stream().toList()
        );

        if (orderItems.size() != requestedQuantities.size()) {
            throw new BusinessException(ErrorCode.INVALID_REFUND_REQUEST);
        }

        Map<Long, OrderItem> orderItemMap = orderItems.stream()
                .collect(Collectors.toMap(OrderItem::getId, Function.identity()));

        List<RefundItemPayload> itemPayloads = requestedQuantities.entrySet().stream()
                .map(entry -> createRefundItemPayload(orderItemMap, entry.getKey(), entry.getValue()))
                .toList();

        int totalRefundAmount = calculateTotalRefundAmount(itemPayloads);
        RefundAmount refundAmount = calculateRefundAmount(order, payment, totalRefundAmount);

        Refund refund = refundRepository.save(Refund.request(
                payment,
                refundAmount.totalRefundAmount(),
                refundAmount.pointRefundAmount(),
                refundAmount.pgRefundAmount(),
                request.getRefundReason()
        ));

        List<RefundItem> refundItems = itemPayloads.stream()
                .map(payload -> RefundItem.create(
                        refund,
                        payload.orderItem(),
                        payload.quantity(),
                        payload.refundAmount()
                ))
                .toList();

        List<RefundItem> savedRefundItems = refundItemRepository.saveAll(refundItems);

        return RefundResponse.from(refund, savedRefundItems);
    }

    private void validateRequest(RefundRequest request) {
        if (request == null) {
            throw new BusinessException(ErrorCode.INVALID_REFUND_REQUEST);
        }

        if (request.getOrderId() == null || request.getRefundReason() == null || request.getRefundReason().isBlank()) {
            throw new BusinessException(ErrorCode.INVALID_REFUND_REQUEST);
        }

        if (request.getRefundItems() == null || request.getRefundItems().isEmpty()) {
            throw new BusinessException(ErrorCode.INVALID_REFUND_REQUEST);
        }
    }

    private void validateOwner(Long userId, Order order) {
        if (userId == null || !order.getUser().getId().equals(userId)) {
            throw new BusinessException(ErrorCode.FORBIDDEN_ORDER);
        }
    }

    private void validatePaymentStatus(Payment payment) {
        PaymentStatus paymentStatus = payment.getPaymentStatus();

        if (paymentStatus != PaymentStatus.PAID && paymentStatus != PaymentStatus.PARTIAL_REFUNDED) {
            throw new BusinessException(ErrorCode.INVALID_REFUND_STATUS);
        }
    }

    private Map<Long, Integer> getRequestedQuantities(List<RefundItemRequest> refundItems) {
        Map<Long, Integer> requestedQuantities = new LinkedHashMap<>();

        for (RefundItemRequest refundItem : refundItems) {
            if (refundItem == null || refundItem.getOrderItemId() == null || refundItem.getQuantity() == null) {
                throw new BusinessException(ErrorCode.INVALID_REFUND_REQUEST);
            }

            int quantity = refundItem.getQuantity();
            if (quantity <= 0) {
                throw new BusinessException(ErrorCode.INVALID_REFUND_QUANTITY);
            }

            int currentQuantity = requestedQuantities.getOrDefault(refundItem.getOrderItemId(), 0);
            int totalQuantity = currentQuantity + quantity;
            if (totalQuantity <= 0) {
                throw new BusinessException(ErrorCode.INVALID_REFUND_QUANTITY);
            }

            requestedQuantities.put(refundItem.getOrderItemId(), totalQuantity);
        }

        return requestedQuantities;
    }

    private RefundItemPayload createRefundItemPayload(
            Map<Long, OrderItem> orderItemMap,
            Long orderItemId,
            int quantity
    ) {
        OrderItem orderItem = orderItemMap.get(orderItemId);

        if (orderItem == null) {
            throw new BusinessException(ErrorCode.INVALID_REFUND_REQUEST);
        }

        int refundAmount = orderItem.calculateRefundAmount(quantity);

        return new RefundItemPayload(orderItem, quantity, refundAmount);
    }

    private int calculateTotalRefundAmount(List<RefundItemPayload> itemPayloads) {
        long totalRefundAmount = itemPayloads.stream()
                .mapToLong(RefundItemPayload::refundAmount)
                .sum();

        if (totalRefundAmount > Integer.MAX_VALUE) {
            throw new BusinessException(ErrorCode.INVALID_REFUND_REQUEST);
        }

        return (int) totalRefundAmount;
    }

    private RefundAmount calculateRefundAmount(Order order, Payment payment, int totalRefundAmount) {
        List<Refund> completedRefunds = refundRepository.findAllByPaymentIdAndRefundStatus(
                payment.getId(),
                RefundStatus.COMPLETED
        );

        int completedTotalRefundAmount = sumRefundAmount(completedRefunds, Refund::getTotalRefundAmount);
        int completedPointRefundAmount = sumRefundAmount(completedRefunds, Refund::getPointRefundAmount);
        int completedPgRefundAmount = sumRefundAmount(completedRefunds, Refund::getPgRefundAmount);

        int remainingTotalAmount = subtractRefundedAmount(order.getTotalProductAmount(), completedTotalRefundAmount);
        int remainingPointAmount = subtractRefundedAmount(order.getUsedPointAmount(), completedPointRefundAmount);
        int remainingPgAmount = subtractRefundedAmount(order.getPgPaymentAmount(), completedPgRefundAmount);

        if (remainingTotalAmount != remainingPointAmount + remainingPgAmount) {
            throw new BusinessException(ErrorCode.INVALID_REFUND_REQUEST);
        }

        if (totalRefundAmount > remainingTotalAmount) {
            throw new BusinessException(ErrorCode.INVALID_REFUND_REQUEST);
        }

        if (remainingTotalAmount == 0) {
            return new RefundAmount(totalRefundAmount, 0, totalRefundAmount);
        }

        if (totalRefundAmount == remainingTotalAmount) {
            return new RefundAmount(totalRefundAmount, remainingPointAmount, remainingPgAmount);
        }

        int pointRefundAmount = (int) ((long) remainingPointAmount * totalRefundAmount / remainingTotalAmount);
        int pgRefundAmount = totalRefundAmount - pointRefundAmount;

        if (pgRefundAmount > remainingPgAmount) {
            int overflowAmount = pgRefundAmount - remainingPgAmount;
            pgRefundAmount = remainingPgAmount;
            pointRefundAmount += overflowAmount;
        }

        if (pointRefundAmount > remainingPointAmount) {
            int overflowAmount = pointRefundAmount - remainingPointAmount;
            pointRefundAmount = remainingPointAmount;
            pgRefundAmount += overflowAmount;
        }

        validateRefundAmount(totalRefundAmount, pointRefundAmount, pgRefundAmount, remainingPointAmount, remainingPgAmount);

        return new RefundAmount(totalRefundAmount, pointRefundAmount, pgRefundAmount);
    }

    private int sumRefundAmount(List<Refund> refunds, Function<Refund, Integer> amountGetter) {
        long sum = refunds.stream()
                .mapToLong(refund -> amountGetter.apply(refund))
                .sum();

        if (sum > Integer.MAX_VALUE) {
            throw new BusinessException(ErrorCode.INVALID_REFUND_REQUEST);
        }

        return (int) sum;
    }

    private int subtractRefundedAmount(int originalAmount, int refundedAmount) {
        int remainingAmount = originalAmount - refundedAmount;

        if (remainingAmount < 0) {
            throw new BusinessException(ErrorCode.INVALID_REFUND_REQUEST);
        }

        return remainingAmount;
    }

    private void validateRefundAmount(
            int totalRefundAmount,
            int pointRefundAmount,
            int pgRefundAmount,
            int remainingPointAmount,
            int remainingPgAmount
    ) {
        if (pointRefundAmount < 0 || pgRefundAmount < 0) {
            throw new BusinessException(ErrorCode.INVALID_REFUND_REQUEST);
        }

        if (pointRefundAmount > remainingPointAmount || pgRefundAmount > remainingPgAmount) {
            throw new BusinessException(ErrorCode.INVALID_REFUND_REQUEST);
        }

        if (totalRefundAmount != pointRefundAmount + pgRefundAmount) {
            throw new BusinessException(ErrorCode.INVALID_REFUND_REQUEST);
        }
    }

    private record RefundItemPayload(
            OrderItem orderItem,
            int quantity,
            int refundAmount
    ) {
    }

    private record RefundAmount(
            int totalRefundAmount,
            int pointRefundAmount,
            int pgRefundAmount
    ) {
    }

}
