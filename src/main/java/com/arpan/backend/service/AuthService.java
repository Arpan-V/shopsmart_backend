package com.arpan.backend.service;

import com.arpan.backend.dto.ApiResponse;
import com.arpan.backend.dto.auth.AuthResponse;
import com.arpan.backend.dto.auth.LoginRequest;
import com.arpan.backend.dto.auth.RegisterRequest;

public interface AuthService {
    ApiResponse<String> register(RegisterRequest request);
    ApiResponse<AuthResponse> login(LoginRequest request);
    ApiResponse<AuthResponse> refresh(String refreshToken);
    ApiResponse<String> logout(String refreshToken);

    void verify(String token);

    String resendVerification(String email);
}