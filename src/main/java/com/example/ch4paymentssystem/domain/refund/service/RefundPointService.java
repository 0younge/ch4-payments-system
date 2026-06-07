package com.example.ch4paymentssystem.domain.refund.service;

import com.example.ch4paymentssystem.domain.order.entity.Order;
import com.example.ch4paymentssystem.domain.point.entity.PointHistory;
import com.example.ch4paymentssystem.domain.point.entity.PointType;
import com.example.ch4paymentssystem.domain.point.repository.PointHistoryRepository;
import com.example.ch4paymentssystem.domain.refund.entity.Refund;
import com.example.ch4paymentssystem.domain.refund.entity.RefundStatus;
import com.example.ch4paymentssystem.domain.refund.repository.RefundRepository;
import com.example.ch4paymentssystem.domain.user.entity.User;
import com.example.ch4paymentssystem.global.exception.BusinessException;
import com.example.ch4paymentssystem.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.function.Function;

@Service
@RequiredArgsConstructor
public class RefundPointService {

    private final RefundRepository refundRepository;
    private final PointHistoryRepository pointHistoryRepository;

    @Transactional
    public void applyRefundPoints(Long refundId) {
        Refund refund = refundRepository.findByIdWithPaymentOrderUser(refundId)
                .orElseThrow(() -> new BusinessException(ErrorCode.REFUND_NOT_FOUND));

        applyRefundPoints(refund);
    }

    private void applyRefundPoints(Refund refund) {
        validateRefund(refund);

        Order order = refund.getPayment().getOrder();
        User user = order.getUser();
        List<Refund> completedRefunds = getCompletedRefunds(refund);
        int refundUsedPointAmount = refund.getPointRefundAmount();
        int cancelEarnedPointAmount = calculateCancelEarnedPointAmount(refund, completedRefunds);

        validateRefundAmount(refund, completedRefunds);
        validatePointBalance(user, refundUsedPointAmount, cancelEarnedPointAmount);

        if (refundUsedPointAmount > 0) {
            user.refundUsedPoint(refundUsedPointAmount);
            pointHistoryRepository.save(PointHistory.create(
                    user,
                    order,
                    PointType.REFUND_USED,
                    refundUsedPointAmount,
                    user.getPointBalance(),
                    "환불 사용 포인트 복구"
            ));
        }

        if (cancelEarnedPointAmount > 0) {
            user.cancelEarnedPoint(cancelEarnedPointAmount);
            pointHistoryRepository.save(PointHistory.create(
                    user,
                    order,
                    PointType.CANCEL_EARNED,
                    -cancelEarnedPointAmount,
                    user.getPointBalance(),
                    "환불 적립 포인트 회수"
            ));
        }
    }

    private void validateRefund(Refund refund) {
        if (refund == null || refund.getPayment() == null || refund.getPayment().getOrder() == null) {
            throw new BusinessException(ErrorCode.INVALID_REFUND_REQUEST);
        }

        if (refund.getRefundStatus() != RefundStatus.REQUESTED) {
            throw new BusinessException(ErrorCode.INVALID_REFUND_STATUS);
        }
    }

    private int calculateCancelEarnedPointAmount(Refund refund, List<Refund> completedRefunds) {
        Order order = refund.getPayment().getOrder();

        if (order.getEarnedPointAmount() == 0 || refund.getPgRefundAmount() == 0) {
            return 0;
        }

        int completedPgRefundAmount = sumRefundAmount(completedRefunds, Refund::getPgRefundAmount);
        int remainingPgAmount = subtractRefundedAmount(order.getPgPaymentAmount(), completedPgRefundAmount);
        int cancelledEarnedPointAmount = getCancelledEarnedPointAmount(order);
        int remainingEarnedPointAmount = subtractRefundedAmount(order.getEarnedPointAmount(), cancelledEarnedPointAmount);

        if (remainingPgAmount == 0) {
            throw new BusinessException(ErrorCode.INVALID_REFUND_REQUEST);
        }

        if (refund.getPgRefundAmount() > remainingPgAmount) {
            throw new BusinessException(ErrorCode.INVALID_REFUND_REQUEST);
        }

        if (refund.getPgRefundAmount().equals(remainingPgAmount)) {
            return remainingEarnedPointAmount;
        }

        return (int) ((long) remainingEarnedPointAmount * refund.getPgRefundAmount() / remainingPgAmount);
    }

    private void validateRefundAmount(Refund refund, List<Refund> completedRefunds) {
        Order order = refund.getPayment().getOrder();
        int completedPointRefundAmount = sumRefundAmount(completedRefunds, Refund::getPointRefundAmount);
        int completedPgRefundAmount = sumRefundAmount(completedRefunds, Refund::getPgRefundAmount);
        int remainingPointAmount = subtractRefundedAmount(order.getUsedPointAmount(), completedPointRefundAmount);
        int remainingPgAmount = subtractRefundedAmount(order.getPgPaymentAmount(), completedPgRefundAmount);

        if (refund.getPointRefundAmount() > remainingPointAmount || refund.getPgRefundAmount() > remainingPgAmount) {
            throw new BusinessException(ErrorCode.INVALID_REFUND_REQUEST);
        }
    }

    private List<Refund> getCompletedRefunds(Refund refund) {
        return refundRepository.findAllByPaymentIdAndRefundStatus(
                refund.getPayment().getId(),
                RefundStatus.COMPLETED
        );
    }

    private int getCancelledEarnedPointAmount(Order order) {
        List<PointHistory> pointHistories = pointHistoryRepository.findAllByOrderIdAndPointType(
                order.getId(),
                PointType.CANCEL_EARNED
        );

        long amount = pointHistories.stream()
                .mapToLong(pointHistory -> {
                    if (pointHistory.getAmount() > 0) {
                        throw new BusinessException(ErrorCode.INVALID_REFUND_REQUEST);
                    }

                    return -pointHistory.getAmount();
                })
                .sum();

        if (amount > Integer.MAX_VALUE) {
            throw new BusinessException(ErrorCode.INVALID_REFUND_REQUEST);
        }

        return (int) amount;
    }

    private int sumRefundAmount(List<Refund> refunds, Function<Refund, Integer> amountGetter) {
        long amount = refunds.stream()
                .mapToLong(refund -> amountGetter.apply(refund))
                .sum();

        if (amount > Integer.MAX_VALUE) {
            throw new BusinessException(ErrorCode.INVALID_REFUND_REQUEST);
        }

        return (int) amount;
    }

    private int subtractRefundedAmount(int originalAmount, int refundedAmount) {
        int remainingAmount = originalAmount - refundedAmount;

        if (remainingAmount < 0) {
            throw new BusinessException(ErrorCode.INVALID_REFUND_REQUEST);
        }

        return remainingAmount;
    }

    private void validatePointBalance(
            User user,
            int refundUsedPointAmount,
            int cancelEarnedPointAmount
    ) {
        long balanceAfterRefundUsedPoint = (long) user.getPointBalance() + refundUsedPointAmount;

        if (balanceAfterRefundUsedPoint > Integer.MAX_VALUE) {
            throw new BusinessException(ErrorCode.INVALID_POINT_AMOUNT);
        }

        if (balanceAfterRefundUsedPoint < cancelEarnedPointAmount) {
            throw new BusinessException(ErrorCode.NOT_ENOUGH_POINT);
        }
    }
}
