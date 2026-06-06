package com.example.ch4paymentssystem.domain.order.service;

import com.example.ch4paymentssystem.domain.cart.entity.Cart;
import com.example.ch4paymentssystem.domain.cart.entity.CartItem;
import com.example.ch4paymentssystem.domain.cart.repository.CartItemRepository;
import com.example.ch4paymentssystem.domain.cart.repository.CartRepository;
import com.example.ch4paymentssystem.domain.order.dto.request.CancelOrderRequest;
import com.example.ch4paymentssystem.domain.order.dto.request.CreateOrderRequest;
import com.example.ch4paymentssystem.domain.order.dto.request.OrderPreviewRequest;
import com.example.ch4paymentssystem.domain.order.dto.response.*;
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
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
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

        if (product.getStock() < quantity) {
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

    @Transactional(readOnly = true)
    public OrderDetailResponse getOrderDetail(Long userId, Long orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new BusinessException(ErrorCode.ORDER_NOT_FOUND));

        if (!order.getUser().getId().equals(userId)) {
            throw new BusinessException(ErrorCode.FORBIDDEN_ORDER);
        }

        Payment payment = paymentRepository.findByOrderId(orderId)
                .orElseThrow(() -> new BusinessException(ErrorCode.PAYMENT_NOT_FOUND));

        List<OrderItem> orderItems = orderItemRepository.findAllByOrderId(orderId);

        List<OrderDetailItemResponse> itemResponses = orderItems.stream()
                .map(item -> new OrderDetailItemResponse(
                        item.getId(),
                        item.getProduct().getId(),
                        item.getProductName(),
                        item.getProductPrice(),
                        item.getQuantity(),
                        item.getProductPrice() * item.getQuantity()
                ))
                .toList();

        OrderPaymentResponse paymentResponse = new OrderPaymentResponse(
                payment.getId(),
                payment.getPortonePaymentId(),
                payment.getPaymentStatus().name(),
                payment.getPaidAt()
        );

        return new OrderDetailResponse(
                order.getId(),
                order.getOrderNumber(),
                order.getOrderStatus().name(),
                order.getTotalProductAmount(),
                order.getUsedPointAmount(),
                payment.getPgAmount(),
                payment.getPoint(),
                order.getCreatedAt(),
                paymentResponse,
                itemResponses
        );
    }

    @Transactional(readOnly = true)
    public OrderListResponse getMyOrders(Long userId, OrderStatus status, int page, int size) {
        if (page < 1) {
            throw new BusinessException(ErrorCode.INVALID_PAGE_REQUEST);
        }

        if (size < 1) {
            throw new BusinessException(ErrorCode.INVALID_PAGE_REQUEST);
        }

        Pageable pageable = PageRequest.of(
                page - 1,
                size,
                Sort.by(Sort.Direction.DESC, "createdAt")
        );

        Page<Order> orders;

        if (status == null) {
            orders = orderRepository.findAllByUserId(userId, pageable);
        } else {
            orders = orderRepository.findAllByUserIdAndStatus(userId, status, pageable);
        }

        List<OrderSummaryResponse> content = orders.getContent().stream()
                .map(order -> {
                    Payment payment = paymentRepository.findByOrderId(order.getId())
                            .orElseThrow(() -> new BusinessException(ErrorCode.PAYMENT_NOT_FOUND));

                    return new OrderSummaryResponse(
                            order.getId(),
                            order.getOrderNumber(),
                            order.getOrderStatus().name(),
                            order.getTotalProductAmount(),
                            order.getUsedPointAmount(),
                            payment.getPgAmount(),
                            payment.getPaymentStatus().name(),
                            order.getCreatedAt()
                    );
                })
                .toList();

        return new OrderListResponse(
                content,
                page,
                size,
                orders.getTotalElements(),
                orders.getTotalPages(),
                orders.hasNext(),
                orders.hasPrevious()
        );
    }

    @Transactional
    public CancelOrderResponse cancelOrder(
            Long userId,
            Long orderId,
            CancelOrderRequest request
    ) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new BusinessException(ErrorCode.ORDER_NOT_FOUND));

        if (!order.getUser().getId().equals(userId)) {
            throw new BusinessException(ErrorCode.FORBIDDEN_ORDER);
        }

        if (order.getOrderStatus() != OrderStatus.PAYMENT_PENDING) {
            throw new BusinessException(ErrorCode.INVALID_ORDER_STATUS);
        }

        Payment payment = paymentRepository.findByOrderId(orderId)
                .orElseThrow(() -> new BusinessException(ErrorCode.PAYMENT_NOT_FOUND));

        if (payment.getPaymentStatus() != PaymentStatus.PENDING) {
            throw new BusinessException(ErrorCode.INVALID_PAYMENT_STATUS);
        }

        List<OrderItem> orderItems = orderItemRepository.findAllByOrderId(orderId);

        List<RestoredStockResponse> restoredStock = orderItems.stream()
                .map(orderItem -> {
                    Product product = orderItem.getProduct();

                    product.increaseStock(orderItem.getQuantity());

                    return new RestoredStockResponse(
                            product.getId(),
                            orderItem.getProductName(),
                            orderItem.getQuantity()
                    );
                })
                .toList();

        order.cancel();
        payment.fail();

        return new CancelOrderResponse(
                order.getId(),
                order.getOrderNumber(),
                order.getOrderStatus().name(),
                payment.getId(),
                payment.getPaymentStatus().name(),
                restoredStock,
                LocalDateTime.now()
        );
    }

    @Transactional(readOnly = true)
    public OrderPreviewResponse previewOrder(
            Long userId,
            OrderPreviewRequest request
    ) {
        Cart cart = cartRepository.findByUserId(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.CART_NOT_FOUND));

        List<CartItem> cartItems = getPreviewCartItems(
                cart.getId(),
                request.getCartItemIds()
        );

        if (cartItems.isEmpty()) {
            throw new BusinessException(ErrorCode.CART_ITEM_NOT_FOUND);
        }

        List<OrderPreviewItemResponse> items = cartItems.stream()
                .map(this::toPreviewItemResponse)
                .toList();

        int totalAmount = items.stream()
                .mapToInt(OrderPreviewItemResponse::getItemTotalAmount)
                .sum();

        return new OrderPreviewResponse(
                items,
                totalAmount
        );
    }

    private List<CartItem> getPreviewCartItems(
            Long cartId,
            List<Long> cartItemIds
    ) {
        if (cartItemIds == null || cartItemIds.isEmpty()) {
            return cartItemRepository.findAllByCartId(cartId);
        }

        return cartItemRepository.findAllByIdInAndCartId(
                cartItemIds,
                cartId
        );
    }

    private OrderPreviewItemResponse toPreviewItemResponse(
            CartItem cartItem
    ) {
        Product product = cartItem.getProduct();

        validateProduct(
                product,
                cartItem.getQuantity()
        );

        int itemTotalAmount =
                product.getPrice() * cartItem.getQuantity();

        return new OrderPreviewItemResponse(
                cartItem.getId(),
                product.getId(),
                product.getName(),
                product.getPrice(),
                cartItem.getQuantity(),
                product.getStock(),
                itemTotalAmount
        );
    }
}