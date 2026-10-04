package com.fitkart.service.impl;

import com.fitkart.exception.ProductUnavailableException;
import com.fitkart.exception.StockUnavailableException;
import com.fitkart.dto.cart.AddCartItemRequest;
import com.fitkart.dto.cart.CartItemResponse;
import com.fitkart.dto.cart.CartResponse;
import com.fitkart.dto.cart.UpdateCartItemRequest;
import com.fitkart.entity.Cart;
import com.fitkart.entity.CartItem;
import com.fitkart.entity.Product;
import com.fitkart.entity.ProductStatus;
import com.fitkart.entity.User;
import com.fitkart.exception.ResourceNotFoundException;
import com.fitkart.exception.UnauthorizedAccessException;
import com.fitkart.repository.CartItemRepository;
import com.fitkart.repository.CartRepository;
import com.fitkart.repository.ProductRepository;
import com.fitkart.repository.UserRepository;
import com.fitkart.service.CartService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class CartServiceImpl implements CartService {

    private final CartRepository cartRepository;
    private final CartItemRepository cartItemRepository;
    private final ProductRepository productRepository;
    private final UserRepository userRepository;

    @Override
    public CartResponse getCart() {

        Cart cart = getOrCreateCart();

        return mapToCartResponse(cart);
    }

    @Override
    public CartResponse addItem(AddCartItemRequest request) {

        Cart cart = getOrCreateCart();

        Product product = productRepository.findById(request.getProductId())
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Product not found with id: "
                                        + request.getProductId()
                        )
                );

        validateProduct(product);

        CartItem existingItem = cartItemRepository
                .findByCartAndProduct(cart, product)
                .orElse(null);

        int newQuantity;

        if (existingItem != null) {

            newQuantity = existingItem.getQuantity()
                    + request.getQuantity();

            validateStock(product, newQuantity);

            existingItem.setQuantity(newQuantity);

            cartItemRepository.save(existingItem);

        } else {

            validateStock(product, request.getQuantity());

            CartItem cartItem = new CartItem();

            cartItem.setCart(cart);
            cartItem.setProduct(product);
            cartItem.setQuantity(request.getQuantity());
            cartItem.setAddedAt(LocalDateTime.now());

            cartItemRepository.save(cartItem);

            cart.getCartItems().add(cartItem);
        }

        return mapToCartResponse(cart);
    }

    @Override
    public CartResponse updateItem(
            Long cartItemId,
            UpdateCartItemRequest request
    ) {

        Cart cart = getOrCreateCart();

        CartItem cartItem = cartItemRepository.findById(cartItemId)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Cart item not found with id: "
                                        + cartItemId
                        )
                );

        if (!cartItem.getCart().getId().equals(cart.getId())) {

            throw new UnauthorizedAccessException(
                    "Cart item does not belong to the current user"
            );
        }

        Product product = cartItem.getProduct();

        validateProduct(product);
        validateStock(product, request.getQuantity());

        cartItem.setQuantity(request.getQuantity());

        cartItemRepository.save(cartItem);

        return mapToCartResponse(cart);
    }

    @Override
    public void removeItem(Long cartItemId) {

        Cart cart = getOrCreateCart();

        CartItem cartItem = cartItemRepository.findById(cartItemId)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Cart item not found with id: "
                                        + cartItemId
                        )
                );

        if (!cartItem.getCart().getId().equals(cart.getId())) {

            throw new UnauthorizedAccessException(
                    "Cart item does not belong to the current user"
            );
        }

        cartItemRepository.delete(cartItem);
    }

    @Override
    public void clearCart() {

        Cart cart = getOrCreateCart();

        cart.getCartItems().clear();

        cartRepository.save(cart);
    }

    private Cart getOrCreateCart() {

        User user = getCurrentUser();

        return cartRepository.findByUser(user)
                .orElseGet(() -> {

                    Cart cart = new Cart();

                    cart.setUser(user);

                    return cartRepository.save(cart);
                });
    }

    private User getCurrentUser() {

        Authentication authentication =
                SecurityContextHolder.getContext()
                        .getAuthentication();

        if (authentication == null ||
                !authentication.isAuthenticated()) {

            throw new UnauthorizedAccessException(
                    "User is not authenticated"
            );
        }

        String email = authentication.getName();

        return userRepository.findByEmail(email)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "User not found with email: "
                                        + email
                        )
                );
    }

    private void validateProduct(Product product) {

        if (product.getStatus() != ProductStatus.ACTIVE) {

            throw new ProductUnavailableException(
                    "Product is not available for purchase"
            );
        }
    }

    private void validateStock(
            Product product,
            int quantity
    ) {

        if (quantity > product.getStockQuantity()) {

            throw new StockUnavailableException(
                    "Requested quantity exceeds available stock"
            );
        }
    }

    private CartResponse mapToCartResponse(Cart cart) {

        List<CartItemResponse> items = cart.getCartItems()
                .stream()
                .map(this::mapToCartItemResponse)
                .toList();

        BigDecimal total = items.stream()
                .map(CartItemResponse::getSubtotal)
                .reduce(
                        BigDecimal.ZERO,
                        BigDecimal::add
                );

        CartResponse response = new CartResponse();

        response.setCartId(cart.getId());
        response.setItems(items);
        response.setTotal(total);

        return response;
    }

    private CartItemResponse mapToCartItemResponse(
            CartItem cartItem
    ) {

        Product product = cartItem.getProduct();

        BigDecimal price = product.getPrice();

        BigDecimal subtotal = price.multiply(
                BigDecimal.valueOf(
                        cartItem.getQuantity()
                )
        );

        CartItemResponse response = new CartItemResponse();

        response.setId(cartItem.getId());
        response.setProductId(product.getId());
        response.setProductName(product.getName());
        response.setPrice(price);
        response.setQuantity(cartItem.getQuantity());
        response.setSubtotal(subtotal);

        return response;
    }
}