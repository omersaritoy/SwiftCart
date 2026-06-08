package com.cavcav.swiftcart.auth.service;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.util.Base64;
import java.util.Date;
import java.util.Map;

@Service
@Slf4j
public class JwtService {

    @Value("${jwt.access-secret}")
    private String accessSecret;

    @Value("${jwt.refresh-secret}")
    private String refreshSecret;

    @Value("${jwt.access-expiration}")
    private long accessExpiration;

    @Value("${jwt.refresh-expiration}")
    private long refreshExpiration;


    public String generateAccessToken(String email, String userId, String role) {
        return buildToken(
                Map.of("role", role, "userId", userId, "type", "ACCESS"),
                email,
                accessExpiration,
                getAccessSigningKey()
        );
    }

    public String generateRefreshToken(String email, String userId) {
        return buildToken(
                Map.of("userId", userId, "type", "REFRESH"),
                email,
                refreshExpiration,
                getRefreshSigningKey()
        );
    }

    //validate token
    public boolean isAccessTokenValid(String token) {
        return isTokenValid(token, getAccessSigningKey());
    }

    public boolean isRefreshTokenValid(String token) {
        return isTokenValid(token, getRefreshSigningKey());
    }

    private boolean isTokenValid(String token, SecretKey key) {
        try {
            Claims claims = extractAllClaims(token, key);
            boolean notExpired = claims.getExpiration().after(new Date());
            if (!notExpired) {
                log.warn("Token has expired");
            }
            return notExpired;
        } catch (JwtException e) {
            log.warn("Invalid token: {}", e.getMessage());
            return false;
        }
    }

    //parse token
    public String extractEmailFromAccessToken(String token) {
        return extractAllClaims(token, getAccessSigningKey()).getSubject();
    }

    public String extractEmailFromRefreshToken(String token) {
        return extractAllClaims(token, getRefreshSigningKey()).getSubject();
    }

    public String extractUserIdFromAccessToken(String token) {
        return extractAllClaims(token, getAccessSigningKey()).get("userId", String.class);
    }

    public String extractRoleFromAccessToken(String token) {
        return extractAllClaims(token, getAccessSigningKey()).get("role", String.class);
    }

    public String extractTokenType(String token) {
        try {
            return extractAllClaims(token, getAccessSigningKey()).get("type", String.class);
        } catch (Exception e) {
            return extractAllClaims(token, getRefreshSigningKey()).get("type", String.class);
        }
    }

    public long getAccessTokenExpiration(String token) {
        return extractAllClaims(token, getAccessSigningKey())
                .getExpiration().getTime() - System.currentTimeMillis();
    }

    public long getRefreshTokenExpiration(String token) {
        return extractAllClaims(token, getRefreshSigningKey())
                .getExpiration().getTime() - System.currentTimeMillis();
    }

    private Claims extractAllClaims(String token, SecretKey key) {
        return Jwts.parser()
                .verifyWith(key)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    private String buildToken(Map<String, Object> claims, String subject, long expiration, SecretKey key) {
        return Jwts.builder()
                .claims(claims)
                .subject(subject)
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + expiration))
                .signWith(key)
                .compact();
    }

    private SecretKey getAccessSigningKey() {
        byte[] encodedKey = Base64.getDecoder().decode(accessSecret);
        return Keys.hmacShaKeyFor(encodedKey);
    }

    private SecretKey getRefreshSigningKey() {
        byte[] encodedKey = Base64.getEncoder().encode(refreshSecret.getBytes());
        return Keys.hmacShaKeyFor(encodedKey);
    }
}
