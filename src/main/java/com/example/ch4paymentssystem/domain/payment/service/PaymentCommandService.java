package com.example.ch4paymentssystem.domain.payment.service;

import com.example.ch4paymentssystem.domain.cart.entity.Cart;
import com.example.ch4paymentssystem.domain.cart.repository.CartItemRepository;
import com.example.ch4paymentssystem.domain.cart.repository.CartRepository;
import com.example.ch4paymentssystem.domain.order.entity.Order;
import com.example.ch4paymentssystem.domain.order.entity.OrderItem;
import com.example.ch4paymentssystem.domain.order.repository.OrderItemRepository;
import com.example.ch4paymentssystem.domain.payment.dto.PaymentConfirmResponse;
import com.example.ch4paymentssystem.domain.payment.entity.Payment;
import com.example.ch4paymentssystem.domain.payment.entity.PaymentStatus;
import com.example.ch4paymentssystem.domain.payment.repository.PaymentRepository;
import com.example.ch4paymentssystem.domain.point.entity.PointHistory;
import com.example.ch4paymentssystem.domain.point.entity.PointType;
import com.example.ch4paymentssystem.domain.point.repository.PointHistoryRepository;
import com.example.ch4paymentssystem.domain.product.entity.Product;
import com.example.ch4paymentssystem.domain.user.entity.User;
import com.example.ch4paymentssystem.global.exception.BusinessException;
import com.example.ch4paymentssystem.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class PaymentCommandService {

    private final PaymentRepository paymentRepository;
    private final OrderItemRepository orderItemRepository;
    private final CartRepository cartRepository;
    private final CartItemRepository cartItemRepository;
    private final PointHistoryRepository pointHistoryRepository;

    @Transactional
    public PaymentConfirmResponse completePayment(Long paymentId) {
        Payment payment = findPayment(paymentId);

        if (payment.getPaymentStatus() == PaymentStatus.PAID) {
            return PaymentConfirmResponse.from(payment);
        }

        if (payment.getPaymentStatus() != PaymentStatus.READY) {
            throw new BusinessException(ErrorCode.INVALID_PAYMENT_STATUS);
        }

        Order order = payment.getOrder();
        User user = order.getUser();

        usePoint(user, order);

        order.pay();
        payment.pay();

        earnPoint(user, order);
        deleteOrderedCartItems(order);

        return PaymentConfirmResponse.from(payment);
    }

    @Transactional
    public void failPaymentAndOrder(Long paymentId, String failReason) {
        Payment payment = findPayment(paymentId);

        if (payment.getPaymentStatus() == PaymentStatus.FAILED) {
            return;
        }

        if (payment.getPaymentStatus() != PaymentStatus.READY) {
            throw new BusinessException(ErrorCode.INVALID_PAYMENT_STATUS);
        }

        Order order = payment.getOrder();
        List<OrderItem> orderItems = orderItemRepository.findAllByOrderId(order.getId());

        for (OrderItem orderItem : orderItems) {
            Product product = orderItem.getProduct();
            product.increaseStock(orderItem.getQuantity());
        }

        order.cancel();
        payment.fail(failReason);
    }

    private Payment findPayment(Long paymentId) {
        return paymentRepository.findByIdWithOrderAndUser(paymentId)
                .orElseThrow(() -> new BusinessException(ErrorCode.PAYMENT_NOT_FOUND));
    }

    private void usePoint(User user, Order order) {
        int usedPointAmount = order.getUsedPointAmount();

        if (usedPointAmount == 0) {
            return;
        }

        user.usePoint(usedPointAmount);
        pointHistoryRepository.save(PointHistory.create(
                user,
                order,
                PointType.USE,
                -usedPointAmount,
                user.getPointBalance(),
                "주문 결제 포인트 사용"
        ));
    }

    private void earnPoint(User user, Order order) {
        int earnedPointAmount = order.getEarnedPointAmount();

        if (earnedPointAmount == 0) {
            return;
        }

        user.earnPoint(earnedPointAmount);
        pointHistoryRepository.save(PointHistory.create(
                user,
                order,
                PointType.EARN,
                earnedPointAmount,
                user.getPointBalance(),
                "결제 완료 포인트 적립"
        ));
    }

    private void deleteOrderedCartItems(Order order) {
        Cart cart = cartRepository.findByUserId(order.getUser().getId())
                .orElse(null);

        if (cart == null) {
            return;
        }

        List<Long> productIds = orderItemRepository.findAllByOrderId(order.getId()).stream()
                .map(orderItem -> orderItem.getProduct().getId())
                .toList();

        if (productIds.isEmpty()) {
            return;
        }

        cartItemRepository.deleteAllByCartIdAndProductIdIn(cart.getId(), productIds);
    }
}
