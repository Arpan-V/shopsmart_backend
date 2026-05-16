package com.arpan.backend.controller;

import com.arpan.backend.dto.ApiResponse;
import com.arpan.backend.dto.auth.LoginRequest;
import com.arpan.backend.dto.auth.RegisterRequest;
import com.arpan.backend.service.AuthService;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
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

        var authResponse =
                authService.login(request);

        ResponseCookie accessCookie =
                ResponseCookie.from(
                                "accessToken",
                                authResponse.getData()
                                        .getAccessToken()
                        )
                        .httpOnly(true)
                        .secure(true)
                        .path("/")
                        .maxAge(15 * 60)
                        .sameSite("None")
                        .build();

        ResponseCookie refreshCookie =
                ResponseCookie.from(
                                "refreshToken",
                                authResponse.getData()
                                        .getRefreshToken()
                        )
                        .httpOnly(true)
                        .secure(true)
                        .path("/")
                        .maxAge(7 * 24 * 60 * 60)
                        .sameSite("None")
                        .build();

        return ResponseEntity.ok()
                .header(
                        HttpHeaders.SET_COOKIE,
                        accessCookie.toString()
                )
                .header(
                        HttpHeaders.SET_COOKIE,
                        refreshCookie.toString()
                )
                .body(
                        ApiResponse.success(
                                "Login successful"
                        )
                );
    }

    @PostMapping("/refresh")
    public ResponseEntity<ApiResponse<String>> refresh(
            HttpServletRequest request
    ) {

        String refreshToken =
                extractCookie(
                        request,
                        "refreshToken"
                );

        var response =
                authService.refresh(refreshToken);

        ResponseCookie accessCookie =
                ResponseCookie.from(
                                "accessToken",
                                response.getData()
                                        .getAccessToken()
                        )
                        .httpOnly(true)
                        .secure(true)
                        .path("/")
                        .maxAge(15 * 60)
                        .sameSite("None")
                        .build();

        return ResponseEntity.ok()
                .header(
                        HttpHeaders.SET_COOKIE,
                        accessCookie.toString()
                )
                .body(
                        ApiResponse.success(
                                "Token refreshed"
                        )
                );
    }

    @PostMapping("/logout")
    public ResponseEntity<ApiResponse<String>> logout(
            HttpServletRequest request
    ) {

        String refreshToken =
                extractCookie(
                        request,
                        "refreshToken"
                );

        authService.logout(refreshToken);

        ResponseCookie accessCookie =
                ResponseCookie.from(
                                "accessToken",
                                ""
                        )
                        .httpOnly(true)
                        .secure(true)
                        .path("/")
                        .maxAge(0)
                        .sameSite("None")
                        .build();

        ResponseCookie refreshCookie =
                ResponseCookie.from(
                                "refreshToken",
                                ""
                        )
                        .httpOnly(true)
                        .secure(true)
                        .path("/")
                        .maxAge(0)
                        .sameSite("None")
                        .build();

        return ResponseEntity.ok()
                .header(
                        HttpHeaders.SET_COOKIE,
                        accessCookie.toString()
                )
                .header(
                        HttpHeaders.SET_COOKIE,
                        refreshCookie.toString()
                )
                .body(
                        ApiResponse.success(
                                "Logged out successfully"
                        )
                );
    }

    @GetMapping("/verify")
    void verify(
            @RequestParam String token,
            HttpServletResponse response
    ) throws IOException {

        authService.verify(token);

        response.sendRedirect(
                frontendUrl +
                        "/auth?verified=1"
        );
    }

    @PostMapping("/resend-verification")
    public ResponseEntity<String> resendVerification(
            @RequestParam String email
    ) {

        return ResponseEntity.ok(
                authService.resendVerification(email)
        );
    }

    private String extractCookie(
            HttpServletRequest request,
            String cookieName
    ) {

        if (request.getCookies() == null) {
            return null;
        }

        for (Cookie cookie : request.getCookies()) {

            if (
                    cookie.getName()
                            .equals(cookieName)
            ) {

                return cookie.getValue();
            }
        }

        return null;
    }
}