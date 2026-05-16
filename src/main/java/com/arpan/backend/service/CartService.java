package com.arpan.backend.service;

import com.arpan.backend.dto.cart.AddToCartRequest;
import com.arpan.backend.dto.cart.CartResponse;
import com.arpan.backend.dto.cart.UpdateCartItemRequest;

public interface CartService {

    CartResponse getCart();

    CartResponse addToCart(AddToCartRequest request);

    CartResponse updateQuantity(
            Long cartItemId,
            UpdateCartItemRequest request
    );

    CartResponse removeItem(Long cartItemId);

    void clearCart();
}