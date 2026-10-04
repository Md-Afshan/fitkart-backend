package com.fitkart.repository;

import com.fitkart.entity.Cart;
import com.fitkart.entity.CartItem;
import com.fitkart.entity.Product;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface CartItemRepository extends JpaRepository<CartItem, Long> {

    Optional<CartItem> findByCartAndProduct(Cart cart, Product product);
}