package com.arpan.backend.service.impl;

import com.arpan.backend.dto.UserRequest;
import com.arpan.backend.dto.UserResponse;
import com.arpan.backend.entity.Users;
import com.arpan.backend.repository.UserRepo;
import com.arpan.backend.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.Objects;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepo userRepository;

    public UserResponse getCurrentUser() {

        String username = Objects.requireNonNull(SecurityContextHolder.getContext()
                        .getAuthentication())
                .getName();

        Users user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));

        return new UserResponse(
                user.getId(),
                user.getUsername(),
                user.getEmail(),
                user.getRole(),
                user.isEnabled(),
                user.getAddress()
        );
    }

}
