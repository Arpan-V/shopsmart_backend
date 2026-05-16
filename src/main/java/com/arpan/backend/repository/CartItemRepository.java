package com.arpan.backend.repository;

import com.arpan.backend.entity.Cart;
import com.arpan.backend.entity.CartItem;
import com.arpan.backend.entity.Products;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface CartItemRepository
        extends JpaRepository<CartItem, Long> {

    Optional<CartItem> findByCartAndProduct(
            Cart cart,
            Products product
    );
}