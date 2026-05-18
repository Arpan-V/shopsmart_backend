package com.arpan.backend.filter;

import com.arpan.backend.service.impl.CustomUserDetailsService;
import com.arpan.backend.service.impl.JWTService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.jspecify.annotations.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@RequiredArgsConstructor
@Component
public class JwtFilter extends OncePerRequestFilter {

    private final JWTService jwtService;
    // Inject the service directly instead of ApplicationContext
    private final CustomUserDetailsService userDetailsService;

    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain
    ) throws ServletException, IOException {

        // 1. Extract token using Spring Utility
        String token =
                extractTokenFromCookies(request);

        // 2. If no token, just move to the next filter
        if (token == null) {
            filterChain.doFilter(request, response);
            return;
        }

        String username;
        try {
            username = jwtService.extractUserName(token);
        } catch (Exception e) {
            // Token is malformed or expired; just continue as anonymous
            SecurityContextHolder.clearContext();
            filterChain.doFilter(request, response);
            return;
        }

        // 3. Authenticate if not already authenticated
        if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {
            UserDetails userDetails = userDetailsService.loadUserByUsername(username);

            if (jwtService.validateToken(token, userDetails)) {
                UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                        userDetails, null, userDetails.getAuthorities()
                );

                authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                SecurityContextHolder.getContext().setAuthentication(authToken);
            }
        }

        filterChain.doFilter(request, response);
    }


    private String extractTokenFromCookies(
            HttpServletRequest request
    ) {

        if (request.getCookies() == null) {
            return null;
        }

        for (Cookie cookie : request.getCookies()) {

            if (cookie.getName().equals("accessToken")) {
                return cookie.getValue();
            }
        }

        return null;
    }
}