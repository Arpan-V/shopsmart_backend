package com.arpan.backend.service.impl;

import com.arpan.backend.dto.ApiResponse;
import com.arpan.backend.dto.auth.AuthResponse;
import com.arpan.backend.dto.auth.LoginRequest;
import com.arpan.backend.dto.auth.RegisterRequest;
import com.arpan.backend.entity.AuthProvider;
import com.arpan.backend.entity.RefreshToken;
import com.arpan.backend.entity.Users;
import com.arpan.backend.entity.VerificationToken;
import com.arpan.backend.exception.AuthException;
import com.arpan.backend.exception.ConflictException;
import com.arpan.backend.repository.RefreshTokenRepository;
import com.arpan.backend.repository.UserRepo;
import com.arpan.backend.repository.VerificationTokenRepository;
import com.arpan.backend.service.AuthService;

import lombok.RequiredArgsConstructor;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final UserRepo userRepository;

    private final PasswordEncoder passwordEncoder;

    private final AuthenticationManager authenticationManager;

    private final JWTService jwtService;

    private final RefreshTokenRepository refreshTokenRepository;

    private final CustomUserDetailsService userDetailsService;

    private final VerificationTokenRepository verificationTokenRepository;

    private final EmailService emailService;

    private static final Logger logger =
            LoggerFactory.getLogger(AuthServiceImpl.class);

    @Value("${app.backend-url}")
    private String backendUrl;

    @Override
    public ApiResponse<String> register(
            RegisterRequest request
    ) {

        logger.info(
                "Register request received for username: {}",
                request.getUsername()
        );

        // Username exists
        if (
                userRepository.findByUsername(
                        request.getUsername()
                ).isPresent()
        ) {

            throw new ConflictException(
                    "Username already exists"
            );
        }

        // Email exists
        if (
                userRepository.findByEmail(
                        request.getEmail()
                ).isPresent()
        ) {

            throw new ConflictException(
                    "Email already exists"
            );
        }

        Users user =
                Users.builder()
                        .username(
                                request.getUsername()
                        )
                        .email(
                                request.getEmail()
                        )
                        .password(
                                passwordEncoder.encode(
                                        request.getPassword()
                                )
                        )
                        .role("ROLE_USER")
                        .provider(AuthProvider.LOCAL)
                        .enabled(false)
                        .build();

        userRepository.save(user);

        // Generate verification token
        String token =
                UUID.randomUUID().toString();

        VerificationToken verificationToken =
                VerificationToken.builder()
                        .token(token)
                        .user(user)
                        .expiryDate(
                                LocalDateTime.now()
                                        .plusHours(2)
                        )
                        .lastSentAt(
                                LocalDateTime.now()
                        )
                        .build();

        verificationTokenRepository.save(
                verificationToken
        );

        String link =
                backendUrl +
                        "/api/auth/verify?token=" +
                        token;

        emailService.sendAlert(
                user.getEmail(),
                "Verify your account",
                "Click the link to verify your account for ShopSmart:\n"
                        + link
        );

        return new ApiResponse<>(
                true,
                "User registered successfully",
                null
        );
    }

    @Override
    public ApiResponse<AuthResponse> login(
            LoginRequest request
    ) {

        try {

            logger.info(
                    "Login attempt for username: {}",
                    request.getUsername()
            );

            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            request.getUsername(),
                            request.getPassword()
                    )
            );

            Users user =
                    userRepository.findByUsername(
                                    request.getUsername()
                            )
                            .orElseThrow(() ->
                                    new AuthException(
                                            "User does not exist"
                                    )
                            );

            if (!user.isEnabled()) {

                throw new AuthException(
                        "Please verify your email first"
                );
            }

            String accessToken =
                    jwtService.generateAccessToken(
                            user.getUsername()
                    );

            String refreshToken =
                    jwtService.generateRefreshToken(
                            user.getUsername()
                    );

            RefreshToken tokenEntity =
                    RefreshToken.builder()
                            .token(refreshToken)
                            .expiryDate(
                                    LocalDateTime.now()
                                            .plusDays(7)
                            )
                            .revoked(false)
                            .user(user)
                            .build();

            refreshTokenRepository.save(
                    tokenEntity
            );

            return new ApiResponse<>(
                    true,
                    "Login successful",
                    new AuthResponse(
                            accessToken,
                            refreshToken
                    )
            );

        } catch (Exception e) {

            logger.error(
                    "Login failed for username: {}",
                    request.getUsername()
            );

            return new ApiResponse<>(
                    false,
                    "Invalid credentials",
                    null
            );
        }
    }

    @Override
    public ApiResponse<AuthResponse> refresh(
            String refreshToken
    ) {

        RefreshToken tokenEntity =
                refreshTokenRepository.findByToken(
                                refreshToken
                        )
                        .orElseThrow(() ->
                                new AuthException(
                                        "Invalid refresh token"
                                )
                        );

        // Reuse detection
        if (tokenEntity.isRevoked()) {

            throw new AuthException(
                    "Refresh Token reuse detected, possible attack."
            );
        }

        String username =
                jwtService.extractUserName(
                        refreshToken
                );

        UserDetails userDetails =
                userDetailsService.loadUserByUsername(
                        username
                );

        if (
                !jwtService.validateRefreshToken(
                        refreshToken,
                        userDetails
                )
        ) {

            throw new AuthException(
                    "Invalid refresh token"
            );
        }

        // Revoke old token
        tokenEntity.setRevoked(true);

        refreshTokenRepository.save(
                tokenEntity
        );

        // Generate new refresh token
        String newRefreshToken =
                jwtService.generateRefreshToken(
                        username
                );

        RefreshToken newTokenEntity =
                RefreshToken.builder()
                        .token(newRefreshToken)
                        .expiryDate(
                                LocalDateTime.now()
                                        .plusDays(7)
                        )
                        .revoked(false)
                        .user(tokenEntity.getUser())
                        .build();

        refreshTokenRepository.save(
                newTokenEntity
        );

        // Generate new access token
        String newAccessToken =
                jwtService.generateAccessToken(
                        username
                );

        return new ApiResponse<>(
                true,
                "Token refreshed",
                new AuthResponse(
                        newAccessToken,
                        newRefreshToken
                )
        );
    }

    @Override
    public ApiResponse<String> logout(
            String refreshToken
    ) {

        RefreshToken tokenEntity =
                refreshTokenRepository.findByToken(
                                refreshToken
                        )
                        .orElseThrow(() ->
                                new AuthException(
                                        "Invalid refresh token"
                                )
                        );

        tokenEntity.setRevoked(true);

        refreshTokenRepository.save(
                tokenEntity
        );

        return new ApiResponse<>(
                true,
                "Logged out successfully",
                null
        );
    }

    @Override
    public void verify(String token) {

        VerificationToken verificationToken =
                verificationTokenRepository.findByToken(
                                token
                        )
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "Invalid token"
                                )
                        );

        // Expiry check
        if (
                verificationToken.getExpiryDate()
                        .isBefore(
                                LocalDateTime.now()
                        )
        ) {

            throw new RuntimeException(
                    "Token expired"
            );
        }

        Users user =
                verificationToken.getUser();

        user.setEnabled(true);

        userRepository.save(user);
    }

    @Override
    public String resendVerification(
            String email
    ) {

        Users user =
                userRepository.findByEmail(email)
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "User not found"
                                )
                        );

        // Already verified
        if (user.isEnabled()) {

            throw new RuntimeException(
                    "User already verified"
            );
        }

        VerificationToken existingToken =
                verificationTokenRepository.findByUser(user)
                        .orElse(null);

        // Rate limit
        if (
                existingToken != null &&
                        existingToken.getLastSentAt() != null
        ) {

            LocalDateTime now =
                    LocalDateTime.now();

            long diff =
                    java.time.Duration.between(
                            existingToken.getLastSentAt(),
                            now
                    ).toMillis();

            if (diff < 1000 * 60 * 2) {

                long remaining =
                        (1000 * 60 * 2 - diff) / 1000;

                throw new RuntimeException(
                        "Please wait "
                                + remaining
                                + " seconds before requesting again"
                );
            }

            verificationTokenRepository.findByUser(user)
                    .ifPresent(
                            verificationTokenRepository::delete
                    );
        }

        // Generate new token
        String token =
                UUID.randomUUID().toString();

        VerificationToken verificationToken =
                VerificationToken.builder()
                        .token(token)
                        .user(user)
                        .expiryDate(
                                LocalDateTime.now()
                                        .plusHours(24)
                        )
                        .lastSentAt(
                                LocalDateTime.now()
                        )
                        .build();

        verificationTokenRepository.save(
                verificationToken
        );

        String link =
                backendUrl +
                        "/api/auth/verify?token=" +
                        token;

        emailService.sendAlert(
                user.getEmail(),
                "Resend Verification",
                "Click this link to verify your account:\n"
                        + link
        );

        return "Verification email resent successfully";
    }
}