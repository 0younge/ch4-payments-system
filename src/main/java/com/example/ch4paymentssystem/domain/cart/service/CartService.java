package com.example.ch4paymentssystem.domain.cart.service;

import com.example.ch4paymentssystem.domain.cart.dto.AddCartItemRequest;
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

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class CartService {

    private final CartRepository cartRepository;
    private final CartItemRepository cartItemRepository;
    private final ProductRepository productRepository;

    // 장바구니 추가
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
}