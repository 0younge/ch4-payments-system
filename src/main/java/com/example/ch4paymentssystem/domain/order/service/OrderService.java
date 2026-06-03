package com.example.ch4paymentssystem.domain.order.service;

import com.example.ch4paymentssystem.domain.cart.entity.Cart;
import com.example.ch4paymentssystem.domain.cart.entity.CartItem;
import com.example.ch4paymentssystem.domain.cart.repository.CartItemRepository;
import com.example.ch4paymentssystem.domain.cart.repository.CartRepository;
import com.example.ch4paymentssystem.domain.order.dto.request.CreateOrderRequest;
import com.example.ch4paymentssystem.domain.order.dto.response.CreateOrderItemResponse;
import com.example.ch4paymentssystem.domain.order.dto.response.CreateOrderResponse;
import com.example.ch4paymentssystem.domain.order.entity.Order;
import com.example.ch4paymentssystem.domain.order.entity.OrderItem;
import com.example.ch4paymentssystem.domain.order.entity.OrderStatus;
import com.example.ch4paymentssystem.domain.order.repository.OrderItemRepository;
import com.example.ch4paymentssystem.domain.order.repository.OrderRepository;
import com.example.ch4paymentssystem.domain.payment.entity.Payment;
import com.example.ch4paymentssystem.domain.payment.entity.PaymentStatus;
import com.example.ch4paymentssystem.domain.payment.repository.PaymentRepository;
import com.example.ch4paymentssystem.domain.product.entity.Product;
import com.example.ch4paymentssystem.domain.product.entity.ProductStatus;
import com.example.ch4paymentssystem.domain.user.entity.User;
import com.example.ch4paymentssystem.domain.user.repository.UserRepository;
import com.example.ch4paymentssystem.global.exception.BusinessException;
import com.example.ch4paymentssystem.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class OrderService {

    private final UserRepository userRepository;
    private final CartRepository cartRepository;
    private final CartItemRepository cartItemRepository;
    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;
    private final PaymentRepository paymentRepository;

    @Transactional
    public CreateOrderResponse createOrder(Long userId, CreateOrderRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

        Cart cart = cartRepository.findByUserId(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.CART_NOT_FOUND));

        List<CartItem> cartItems = getOrderCartItems(cart.getId(), request.getCartItemIds());

        if (cartItems.isEmpty()) {
            throw new BusinessException(ErrorCode.CART_ITEM_NOT_FOUND);
        }

        int totalAmount = calculateTotalAmount(cartItems);
        int usedPointAmount = request.getUsedPointAmount();
        validatePoint(user, totalAmount, usedPointAmount);

        String orderNumber = generateOrderNumber();

        Order order = Order.create(
                user,
                orderNumber,
                totalAmount,
                usedPointAmount,
                OrderStatus.PAYMENT_PENDING
        );

        Order savedOrder = orderRepository.save(order);

        List<OrderItem> orderItems = cartItems.stream()
                .map(cartItem -> createOrderItemAndDecreaseStock(savedOrder, cartItem))
                .toList();

        orderItemRepository.saveAll(orderItems);

        int pgAmount = totalAmount - usedPointAmount;
        String portonePaymentId = generatePortonePaymentId();

        Payment payment = Payment.create(
                savedOrder,
                portonePaymentId,
                totalAmount,
                usedPointAmount,
                pgAmount,
                PaymentStatus.READY
        );

        Payment savedPayment = paymentRepository.save(payment);

        List<CreateOrderItemResponse> itemResponses = orderItems.stream()
                .map(item -> new CreateOrderItemResponse(
                        item.getId(),
                        item.getProduct().getId(),
                        item.getProductName(),
                        item.getProductPrice(),
                        item.getQuantity(),
                        item.getProductPrice() * item.getQuantity()
                ))
                .toList();

        return new CreateOrderResponse(
                savedOrder.getId(),
                savedOrder.getOrderNumber(),
                savedOrder.getOrderStatus().name(),
                savedPayment.getId(),
                savedPayment.getPortonePaymentId(),
                savedPayment.getPaymentStatus().name(),
                totalAmount,
                usedPointAmount,
                pgAmount,
                itemResponses
        );
    }

    private List<CartItem> getOrderCartItems(Long cartId, List<Long> cartItemIds) {
        if (cartItemIds == null || cartItemIds.isEmpty()) {
            return cartItemRepository.findAllByCartId(cartId);
        }

        return cartItemRepository.findAllByIdInAndCartId(cartItemIds, cartId);
    }

    private int calculateTotalAmount(List<CartItem> cartItems) {
        return cartItems.stream()
                .mapToInt(cartItem -> {
                    Product product = cartItem.getProduct();
                    validateProduct(product, cartItem.getQuantity());
                    return product.getPrice() * cartItem.getQuantity();
                })
                .sum();
    }

    private OrderItem createOrderItemAndDecreaseStock(Order order, CartItem cartItem) {
        Product product = cartItem.getProduct();
        int quantity = cartItem.getQuantity();

        validateProduct(product, quantity);
        product.decreaseStock(quantity);

        return OrderItem.create(
                order,
                product,
                product.getName(),
                product.getPrice(),
                quantity
        );
    }

    private void validateProduct(Product product, int quantity) {
        if (product.getStatus() != ProductStatus.ON_SALE) {
            throw new BusinessException(ErrorCode.PRODUCT_NOT_ON_SALE);
        }

        if (product.getStockQuantity() < quantity) {
            throw new BusinessException(ErrorCode.OUT_OF_STOCK);
        }
    }

    private void validatePoint(User user, int totalAmount, int usedPointAmount) {
        if (usedPointAmount < 0) {
            throw new BusinessException(ErrorCode.INVALID_POINT_AMOUNT);
        }

        if (usedPointAmount > totalAmount) {
            throw new BusinessException(ErrorCode.INVALID_POINT_AMOUNT);
        }

        if (user.getPointBalance() < usedPointAmount) {
            throw new BusinessException(ErrorCode.NOT_ENOUGH_POINT);
        }
    }

    private String generateOrderNumber() {
        return "ORD-" + LocalDateTime.now().toString().replace("-", "")
                .replace(":", "")
                .replace(".", "")
                + "-" + UUID.randomUUID().toString().substring(0, 8);
    }

    private String generatePortonePaymentId() {
        return "payment-" + UUID.randomUUID();
    }
}