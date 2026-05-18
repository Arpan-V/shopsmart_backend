package com.arpan.backend.security;

import com.arpan.backend.entity.AuthProvider;
import com.arpan.backend.entity.RefreshToken;
import com.arpan.backend.entity.Users;
import com.arpan.backend.repository.RefreshTokenRepository;
import com.arpan.backend.repository.UserRepo;
import com.arpan.backend.service.impl.JWTService;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import lombok.RequiredArgsConstructor;

import org.jspecify.annotations.NonNull;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.time.LocalDateTime;

@Component
@RequiredArgsConstructor
public class OAuth2SuccessHandler implements AuthenticationSuccessHandler {

    private final UserRepo userRepo;
    private final JWTService jwtService;
    private final RefreshTokenRepository refreshTokenRepository;

    @Value("${app.frontend-url}")
    private String frontendUrl;

    @Override
    public void onAuthenticationSuccess(
            @NonNull HttpServletRequest request,
            HttpServletResponse response,
            Authentication authentication
    ) throws IOException, ServletException {

        OAuth2User oauthUser = (OAuth2User) authentication.getPrincipal();
        assert oauthUser != null;
        String email = oauthUser.getAttribute("email");

        Users user = userRepo.findByEmail(email)
                .orElseGet(() -> {
                    assert email != null;
                    String baseUsername = email.split("@")[0].replaceAll("[^A-Za-z0-9]", "");
                    String uniqueUsername = generateUniqueUsername(baseUsername);

                    Users newUser = Users.builder()
                            .email(email)
                            .username(uniqueUsername)
                            .password(null)
                            .enabled(true)
                            .role("ROLE_USER")
                            .provider(AuthProvider.GOOGLE)
                            .build();
                    return userRepo.save(newUser);
                });

        String accessToken = jwtService.generateAccessToken(user.getUsername());
        String refreshToken = jwtService.generateRefreshToken(user.getUsername());

        RefreshToken tokenEntity = RefreshToken.builder()
                .token(refreshToken)
                .expiryDate(LocalDateTime.now().plusDays(7))
                .revoked(false)
                .user(user)
                .build();
        refreshTokenRepository.save(tokenEntity);

        response.addHeader(HttpHeaders.SET_COOKIE,
                createCookie("accessToken", accessToken, 15 * 60).toString());
        response.addHeader(HttpHeaders.SET_COOKIE,
                createCookie("refreshToken", refreshToken, 7 * 24 * 60 * 60).toString());

        response.sendRedirect(frontendUrl + "/oauth-success");
    }

    private String generateUniqueUsername(String base) {
        String username = base;
        int counter = 1;
        while (userRepo.findByUsername(username).isPresent()) {
            username = base + counter;
            counter++;
        }
        return username;
    }

    private ResponseCookie createCookie(String name, String value, long maxAge) {
        return ResponseCookie.from(name, value)
                .httpOnly(true)
                .secure(true)
                .path("/")
                .maxAge(maxAge)
                .sameSite("None")
                .build();
    }
}