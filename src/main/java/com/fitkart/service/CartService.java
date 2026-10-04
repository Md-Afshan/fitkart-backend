package com.fitkart.service;

import com.fitkart.dto.cart.AddCartItemRequest;
import com.fitkart.dto.cart.CartResponse;
import com.fitkart.dto.cart.UpdateCartItemRequest;

public interface CartService {

    CartResponse getCart();

    CartResponse addItem(AddCartItemRequest request);

    CartResponse updateItem(Long cartItemId, UpdateCartItemRequest request);

    void removeItem(Long cartItemId);

    void clearCart();
}