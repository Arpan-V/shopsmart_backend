package com.arpan.backend.filter;

import com.arpan.backend.service.impl.CustomUserDetailsService;
import com.arpan.backend.service.impl.JWTService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationContext;
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
    private final ApplicationContext context;

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {

        String path = request.getServletPath();

        if (
                path.startsWith("/oauth2") ||
                        path.startsWith("/login/oauth2")
        ) {

            filterChain.doFilter(request, response);

            return;
        }

        String token = extractTokenFromCookies(request);
        String username = null;

        if (token != null) {

            try {
                username = jwtService.extractUserName(token);
            } catch (Exception e) {
                filterChain.doFilter(request, response);
                return;
            }
        }

        if (username != null &&
                SecurityContextHolder.getContext()
                        .getAuthentication() == null) {

            UserDetails userDetails =
                    context.getBean(CustomUserDetailsService.class)
                            .loadUserByUsername(username);

            if (jwtService.validateToken(token, userDetails)) {

                UsernamePasswordAuthenticationToken authToken =
                        new UsernamePasswordAuthenticationToken(
                                userDetails,
                                null,
                                userDetails.getAuthorities()
                        );

                authToken.setDetails(
                        new WebAuthenticationDetailsSource()
                                .buildDetails(request)
                );

                SecurityContextHolder.getContext()
                        .setAuthentication(authToken);
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