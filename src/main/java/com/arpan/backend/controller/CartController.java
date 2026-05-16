package com.arpan.backend.controller;

import com.arpan.backend.dto.ApiResponse;
import com.arpan.backend.dto.cart.AddToCartRequest;
import com.arpan.backend.dto.cart.CartResponse;
import com.arpan.backend.dto.cart.UpdateCartItemRequest;
import com.arpan.backend.service.CartService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/cart")
@RequiredArgsConstructor
public class CartController {

    private final CartService cartService;

    // =====================================================
    // GET CART
    // =====================================================

    @GetMapping
    public ResponseEntity<ApiResponse<CartResponse>> getCart() {

        return ResponseEntity.ok(
                new ApiResponse<>(
                        true,
                        "Cart fetched successfully",
                        cartService.getCart()
                )
        );
    }

    // =====================================================
    // ADD TO CART
    // =====================================================

    @PostMapping("/add")
    public ResponseEntity<ApiResponse<CartResponse>> addToCart(
            @Valid
            @RequestBody
            AddToCartRequest request
    ) {

        return ResponseEntity.ok(
                new ApiResponse<>(
                        true,
                        "Product added to cart",
                        cartService.addToCart(request)
                )
        );
    }

    // =====================================================
    // UPDATE QUANTITY
    // =====================================================

    @PutMapping("/item/{cartItemId}")
    public ResponseEntity<ApiResponse<CartResponse>> updateQuantity(
            @PathVariable Long cartItemId,

            @Valid
            @RequestBody
            UpdateCartItemRequest request
    ) {

        return ResponseEntity.ok(
                new ApiResponse<>(
                        true,
                        "Cart updated successfully",
                        cartService.updateQuantity(
                                cartItemId,
                                request
                        )
                )
        );
    }

    // =====================================================
    // REMOVE ITEM
    // =====================================================

    @DeleteMapping("/item/{cartItemId}")
    public ResponseEntity<ApiResponse<CartResponse>> removeItem(
            @PathVariable Long cartItemId
    ) {

        return ResponseEntity.ok(
                new ApiResponse<>(
                        true,
                        "Item removed from cart",
                        cartService.removeItem(cartItemId)
                )
        );
    }

    // =====================================================
    // CLEAR CART
    // =====================================================

    @DeleteMapping("/clear")
    public ResponseEntity<ApiResponse<String>> clearCart() {

        cartService.clearCart();

        return ResponseEntity.ok(
                new ApiResponse<>(
                        true,
                        "Cart cleared successfully",
                        null
                )
        );
    }
}