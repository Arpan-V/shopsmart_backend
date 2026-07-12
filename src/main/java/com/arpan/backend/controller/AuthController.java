package com.arpan.backend.controller;

import com.arpan.backend.dto.ApiResponse;
import com.arpan.backend.dto.auth.AuthResponse;
import com.arpan.backend.dto.auth.LoginRequest;
import com.arpan.backend.dto.auth.RegisterRequest;
import com.arpan.backend.service.AuthService;

import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;

import lombok.RequiredArgsConstructor;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @Value("${app.frontend-url}")
    private String frontendUrl;

    private static final long ACCESS_TOKEN_EXPIRY =
            15 * 60; // 15 mins

    private static final long REFRESH_TOKEN_EXPIRY =
            7 * 24 * 60 * 60; // 7 days

    @PostMapping("/register")
    public ResponseEntity<ApiResponse<String>> register(
            @Valid @RequestBody RegisterRequest request
    ) {

        return ResponseEntity.ok(
                authService.register(request)
        );
    }

    @PostMapping("/login")
    public ResponseEntity<ApiResponse<String>> login(
            @RequestBody LoginRequest request
    ) {

        ApiResponse<AuthResponse> authResponse =
                authService.login(request);

        // Login failed
        if (
                !authResponse.isSuccess() ||
                        authResponse.getData() == null
        ) {

            return ResponseEntity.badRequest()
                    .body(
                            ApiResponse.error(
                                    authResponse.getMessage()
                            )
                    );
        }

        AuthResponse data =
                authResponse.getData();

        return ResponseEntity.ok()
                .headers(headers -> {

                    headers.add(
                            HttpHeaders.SET_COOKIE,
                            createCookie(
                                    "accessToken",
                                    data.getAccessToken(),
                                    ACCESS_TOKEN_EXPIRY
                            ).toString()
                    );

                    headers.add(
                            HttpHeaders.SET_COOKIE,
                            createCookie(
                                    "refreshToken",
                                    data.getRefreshToken(),
                                    REFRESH_TOKEN_EXPIRY
                            ).toString()
                    );
                })
                .body(
                        ApiResponse.success(
                                "Login successful"
                        )
                );
    }

    @PostMapping("/refresh")
    public ResponseEntity<ApiResponse<String>> refresh(
            @CookieValue(
                    name = "refreshToken",
                    required = false
            )
            String refreshToken
    ) {

        if (
                refreshToken == null ||
                        refreshToken.isBlank()
        ) {

            return ResponseEntity.badRequest()
                    .body(
                            ApiResponse.error(
                                    "Refresh token missing"
                            )
                    );
        }

        ApiResponse<AuthResponse> response =
                authService.refresh(refreshToken);

        // Refresh failed
        if (
                !response.isSuccess() ||
                        response.getData() == null
        ) {

            return ResponseEntity.badRequest()
                    .body(
                            ApiResponse.error(
                                    response.getMessage()
                            )
                    );
        }

        AuthResponse data =
                response.getData();

        return ResponseEntity.ok()
                .header(
                        HttpHeaders.SET_COOKIE,
                        createCookie(
                                "accessToken",
                                data.getAccessToken(),
                                ACCESS_TOKEN_EXPIRY
                        ).toString()
                )
                .body(
                        ApiResponse.success(
                                "Token refreshed"
                        )
                );
    }

    @PostMapping("/logout")
    public ResponseEntity<ApiResponse<String>> logout(
            @CookieValue(
                    name = "refreshToken",
                    required = false
            )
            String refreshToken
    ) {

        if (refreshToken != null) {

            authService.logout(refreshToken);
        }

        return ResponseEntity.ok()
                .headers(headers -> {

                    headers.add(
                            HttpHeaders.SET_COOKIE,
                            createCookie(
                                    "accessToken",
                                    "",
                                    0
                            ).toString()
                    );

                    headers.add(
                            HttpHeaders.SET_COOKIE,
                            createCookie(
                                    "refreshToken",
                                    "",
                                    0
                            ).toString()
                    );
                })
                .body(
                        ApiResponse.success(
                                "Logged out successfully"
                        )
                );
    }



    /**
     * Private helper to ensure all security cookies
     * are configured identically.
     */
    private ResponseCookie createCookie(
            String name,
            String value,
            long maxAgeInSeconds
    ) {

        return ResponseCookie.from(name, value)
                .httpOnly(true)
                .secure(true)
                .path("/")
                .maxAge(maxAgeInSeconds)
                .sameSite("None")
                .build();
    }
}