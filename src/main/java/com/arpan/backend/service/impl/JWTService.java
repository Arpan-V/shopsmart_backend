package com.arpan.backend.service.impl;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

@Service
public class JWTService {

    @Value("${jwt.secret}")
    private String secretKey;

    // 🔑 Common generator
    private String generateToken(String username, long expiryMillis, String type) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("type", type);

        LocalDateTime now = LocalDateTime.now();
        LocalDateTime expiryTime = now.plusNanos(expiryMillis * 1_000_000);

        return Jwts.builder()
                .claims()
                .add(claims)
                .subject(username)
                .issuedAt(toDate(now))
                .expiration(toDate(expiryTime))
                .and()
                .signWith(key())
                .compact();
    }

    public String generateAccessToken(String username) {
        return generateToken(username, 1000L * 60 * 15, "access"); // 15 min
    }

    public String generateRefreshToken(String username) {
        return generateToken(username, 1000L * 60 * 60 * 24 * 7, "refresh"); // 7 days
    }

    private SecretKey key() {
        return Keys.hmacShaKeyFor(secretKey.getBytes());
    }

    // 🔄 Convert LocalDateTime → Date
    private Date toDate(LocalDateTime time) {
        return Date.from(time.atZone(ZoneId.systemDefault()).toInstant());
    }

    // 🔄 Convert Date → LocalDateTime
    private LocalDateTime toLocalDateTime(Date date) {
        return date.toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime();
    }

    // 🔍 Extractors
    public String extractUserName(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    public String extractTokenType(String token) {
        return extractClaim(token, claims -> claims.get("type", String.class));
    }

    // ✅ Validators
    public boolean isAccessToken(String token) {
        return "access".equals(extractTokenType(token));
    }

    public boolean isRefreshToken(String token) {
        return "refresh".equals(extractTokenType(token));
    }

    public boolean validateToken(String token, UserDetails userDetails) {
        final String username = extractUserName(token);
        return username.equals(userDetails.getUsername()) && !isTokenExpired(token);
    }

    public boolean validateRefreshToken(String token, UserDetails userDetails) {
        return validateToken(token, userDetails) && isRefreshToken(token);
    }

    private boolean isTokenExpired(String token) {
        return extractExpiration(token).isBefore(LocalDateTime.now());
    }

    private LocalDateTime extractExpiration(String token) {
        Date expirationDate = extractClaim(token, Claims::getExpiration);
        return toLocalDateTime(expirationDate);
    }

    private <T> T extractClaim(String token, Function<Claims, T> resolver) {
        return resolver.apply(extractAllClaims(token));
    }

    private Claims extractAllClaims(String token) {
        return Jwts.parser()
                .verifyWith(key())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }
}