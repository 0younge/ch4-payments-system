package com.example.ch4paymentssystem.domain.cart.service;

import com.example.ch4paymentssystem.domain.cart.dto.AddCartItemRequest;
import com.example.ch4paymentssystem.domain.cart.dto.CartItemResponse;
import com.example.ch4paymentssystem.domain.cart.dto.CartResponse;
import com.example.ch4paymentssystem.domain.cart.dto.UpdateCartItemQuantityRequest;
import com.example.ch4paymentssystem.domain.cart.entity.Cart;
import com.example.ch4paymentssystem.domain.cart.entity.CartItem;
import com.example.ch4paymentssystem.domain.cart.repository.CartItemRepository;
import com.example.ch4paymentssystem.domain.cart.repository.CartRepository;
import com.example.ch4paymentssystem.domain.product.entity.Product;
import com.example.ch4paymentssystem.domain.product.entity.ProductStatus;
import com.example.ch4paymentssystem.domain.product.repository.ProductRepository;
import com.example.ch4paymentssystem.global.exception.BusinessException;
import com.example.ch4paymentssystem.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class CartService {

    private final CartRepository cartRepository;
    private final CartItemRepository cartItemRepository;
    private final ProductRepository productRepository;

    // 장바구니 조회
    @Transactional(readOnly = true)
    public CartResponse getCart(Long userId) {
        Cart cart = cartRepository.findByUserId(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.CART_NOT_FOUND));
        List<CartItem> cartItems = cartItemRepository.findByCartWithProduct(cart);
        List<CartItemResponse> items = cartItems.stream()
                .map(CartItemResponse::from)
                .toList();
        Integer totalAmount = items.stream()
                .mapToInt(CartItemResponse::getItemTotalAmount)
                .sum();
        return new CartResponse(cart.getId(), items, totalAmount);
    }

    // 장바구니 담기
    @Transactional
    public void addCartItem(Long userId, AddCartItemRequest request) {
        Product product = productRepository.findById(request.getProductId())
                .orElseThrow(() -> new BusinessException(ErrorCode.PRODUCT_NOT_FOUND));
        if (product.getStatus() != ProductStatus.ON_SALE) {
            throw new BusinessException(ErrorCode.PRODUCT_NOT_ON_SALE);
        }
        if (product.getStock() < request.getQuantity()) {
            throw new BusinessException(ErrorCode.OUT_OF_STOCK);
        }
        Cart cart = cartRepository.findByUserId(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.CART_NOT_FOUND));
        Optional<CartItem> existingCartItem = cartItemRepository.findByCartAndProduct(cart, product);
        if (existingCartItem.isPresent()) {
            existingCartItem.get().addQuantity(request.getQuantity());
        } else {
            CartItem cartItem = new CartItem(cart, product, request.getQuantity());
            cartItemRepository.save(cartItem);
        }
    }

    // 장바구니 수량 변경
    @Transactional
    public void updateCartItemQuantity(Long userId, Long cartItemId, UpdateCartItemQuantityRequest request) {
        CartItem cartItem = cartItemRepository.findByIdWithCartAndProduct(cartItemId)
                .orElseThrow(() -> new BusinessException(ErrorCode.CART_ITEM_NOT_FOUND));
        if (!cartItem.getCart().getUser().getId().equals(userId)) {
            throw new BusinessException(ErrorCode.FORBIDDEN_RESOURCE);
        }
        if (cartItem.getProduct().getStock() < request.getQuantity()) {
            throw new BusinessException(ErrorCode.OUT_OF_STOCK);
        }
        cartItem.updateQuantity(request.getQuantity());
    }

    // 장바구니 상품 개별 삭제
    @Transactional
    public void deleteCartItem(Long userId, Long cartItemId) {
        CartItem cartItem = cartItemRepository.findByIdWithCartAndProduct(cartItemId)
                .orElseThrow(() -> new BusinessException(ErrorCode.CART_ITEM_NOT_FOUND));
        if (!cartItem.getCart().getUser().getId().equals(userId)) {
            throw new BusinessException(ErrorCode.FORBIDDEN_RESOURCE);
        }
        cartItemRepository.delete(cartItem);
    }
}