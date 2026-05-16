package com.arpan.backend.service.impl;

import com.arpan.backend.dto.cart.*;
import com.arpan.backend.entity.*;
import com.arpan.backend.repository.*;
import com.arpan.backend.service.CartService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class CartServiceImpl implements CartService {

    private final CartRepository cartRepository;

    private final CartItemRepository cartItemRepository;

    private final ProductRepo productRepo;

    private final UserRepo userRepository;

    @Override
    public CartResponse getCart() {

        Users user = getCurrentUser();

        Cart cart = cartRepository.findByUser(user)
                .orElseGet(() -> createCart(user));

        return mapToResponse(cart);
    }

    @Override
    public CartResponse addToCart(
            AddToCartRequest request
    ) {

        Users user = getCurrentUser();

        Cart cart = cartRepository.findByUser(user)
                .orElseGet(() -> createCart(user));

        Products product = productRepo.findById(
                request.getProductId()
        ).orElseThrow(() ->
                new RuntimeException("Product not found")
        );

        if (!product.isProductAvailable()) {

            throw new RuntimeException(
                    "Product is unavailable"
            );
        }

        if (product.getStockQuantity()
                < request.getQuantity()) {

            throw new RuntimeException(
                    "Not enough stock available"
            );
        }

        CartItem cartItem =
                cartItemRepository.findByCartAndProduct(
                        cart,
                        product
                ).orElse(null);

        if (cartItem != null) {

            int newQuantity =
                    cartItem.getQuantity()
                            + request.getQuantity();

            if (product.getStockQuantity()
                    < newQuantity) {

                throw new RuntimeException(
                        "Not enough stock available"
                );
            }

            cartItem.setQuantity(newQuantity);

            cartItem.setSubtotal(
                    product.getPrice().multiply(
                            BigDecimal.valueOf(newQuantity)
                    )
            );

        } else {

            cartItem = CartItem.builder()
                    .cart(cart)
                    .product(product)
                    .quantity(request.getQuantity())
                    .subtotal(
                            product.getPrice().multiply(
                                    BigDecimal.valueOf(
                                            request.getQuantity()
                                    )
                            )
                    )
                    .build();

            cart.getCartItems().add(cartItem);
        }

        recalculateCart(cart);

        cartRepository.save(cart);

        return mapToResponse(cart);
    }

    @Override
    public CartResponse updateQuantity(
            Long cartItemId,
            UpdateCartItemRequest request
    ) {

        Users user = getCurrentUser();

        CartItem cartItem =
                cartItemRepository.findById(cartItemId)
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "Cart item not found"
                                )
                        );

        if (!cartItem.getCart()
                .getUser()
                .getId()
                .equals(user.getId())) {

            throw new RuntimeException(
                    "Unauthorized"
            );
        }

        Products product = cartItem.getProduct();

        if (product.getStockQuantity()
                < request.getQuantity()) {

            throw new RuntimeException(
                    "Not enough stock available"
            );
        }

        cartItem.setQuantity(
                request.getQuantity()
        );

        cartItem.setSubtotal(
                product.getPrice().multiply(
                        BigDecimal.valueOf(
                                request.getQuantity()
                        )
                )
        );

        recalculateCart(cartItem.getCart());

        cartRepository.save(cartItem.getCart());

        return mapToResponse(cartItem.getCart());
    }

    @Override
    public CartResponse removeItem(Long cartItemId) {

        Users user = getCurrentUser();

        CartItem cartItem =
                cartItemRepository.findById(cartItemId)
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "Cart item not found"
                                )
                        );

        if (!cartItem.getCart()
                .getUser()
                .getId()
                .equals(user.getId())) {

            throw new RuntimeException(
                    "Unauthorized"
            );
        }

        Cart cart = cartItem.getCart();

        cart.getCartItems().remove(cartItem);

        cartItemRepository.delete(cartItem);

        recalculateCart(cart);

        cartRepository.save(cart);

        return mapToResponse(cart);
    }

    @Override
    public void clearCart() {

        Users user = getCurrentUser();

        Cart cart = cartRepository.findByUser(user)
                .orElseThrow(() ->
                        new RuntimeException(
                                "Cart not found"
                        )
                );

        cart.getCartItems().clear();

        cart.setTotalPrice(BigDecimal.ZERO);

        cartRepository.save(cart);
    }

    /* ---------------- HELPERS ---------------- */

    private Users getCurrentUser() {

        Authentication authentication =
                SecurityContextHolder.getContext()
                        .getAuthentication();

        String email =
                authentication.getName();

        return userRepository.findByUsername(email)
                .orElseThrow(() ->
                        new RuntimeException(
                                "User not found"
                        )
                );
    }

    private Cart createCart(Users user) {

        Cart cart = Cart.builder()
                .user(user)
                .totalPrice(BigDecimal.ZERO)
                .build();

        return cartRepository.save(cart);
    }

    private void recalculateCart(Cart cart) {

        BigDecimal total = cart.getCartItems()
                .stream()
                .map(CartItem::getSubtotal)
                .reduce(
                        BigDecimal.ZERO,
                        BigDecimal::add
                );

        cart.setTotalPrice(total);
    }

    private CartResponse mapToResponse(Cart cart) {

        List<CartItemResponse> items =
                cart.getCartItems()
                        .stream()
                        .map(item -> CartItemResponse.builder()
                                .cartItemId(item.getId())
                                .productId(
                                        item.getProduct()
                                                .getProdId()
                                )
                                .productName(
                                        item.getProduct()
                                                .getName()
                                )
                                .price(
                                        item.getProduct()
                                                .getPrice()
                                )
                                .quantity(
                                        item.getQuantity()
                                )
                                .subtotal(
                                        item.getSubtotal()
                                )
                                .imageName(
                                        item.getProduct()
                                                .getImageName()
                                )
                                .build()
                        )
                        .toList();

        int totalItems = items.stream()
                .mapToInt(
                        CartItemResponse::getQuantity
                )
                .sum();

        return CartResponse.builder()
                .cartId(cart.getId())
                .items(items)
                .totalPrice(cart.getTotalPrice())
                .totalItems(totalItems)
                .build();
    }
}