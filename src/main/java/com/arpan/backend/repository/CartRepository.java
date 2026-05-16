package com.arpan.backend.repository;

import com.arpan.backend.entity.Cart;
import com.arpan.backend.entity.Users;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface CartRepository
        extends JpaRepository<Cart, Long> {

    Optional<Cart> findByUser(Users user);
}