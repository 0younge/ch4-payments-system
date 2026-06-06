package com.example.ch4paymentssystem.domain.cart.repository;

import com.example.ch4paymentssystem.domain.cart.entity.Cart;
import com.example.ch4paymentssystem.domain.cart.entity.CartItem;
import com.example.ch4paymentssystem.domain.product.entity.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface CartItemRepository extends JpaRepository<CartItem, Long> {
    Optional<CartItem> findByCartAndProduct(Cart cart, Product product);

    List<CartItem> findAllByCartId(Long cartId);

    List<CartItem> findAllByIdInAndCartId(List<Long> cartItemIds, Long cartId);

    @Query("select ci from CartItem ci join fetch ci.product where ci.cart = :cart")
    List<CartItem> findByCartWithProduct(Cart cart);

    @Query("select ci from CartItem ci join fetch ci.cart c join fetch c.user join fetch ci.product where ci.id = :cartItemId")
    Optional<CartItem> findByIdWithCartAndProduct(@Param("cartItemId") Long cartItemId);

    void deleteAllByCart(Cart cart);

    void deleteAllByCartIdAndProductIdIn(Long cartId, List<Long> productIds);
}
