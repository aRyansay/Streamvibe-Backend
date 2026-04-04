package com.streamvibe.security;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.security.Key;
import java.util.Date;

/**
 * Generates, validates, and parses JWT tokens.
 *
 * Token payload:
 *   sub  = user email
 *   id   = user database id (custom claim)
 *   iat  = issued-at
 *   exp  = expiry (default 24h, configurable via jwt.expiration-ms)
 */
@Component
public class JwtUtil {

    @Value("${jwt.secret}")
    private String secret;

    @Value("${jwt.expiration-ms}")
    private long expirationMs;

    private Key signingKey() {
        return Keys.hmacShaKeyFor(secret.getBytes());
    }

    /**
     * Create a JWT for the given user.
     */
    public String generateToken(Long userId, String email) {
        return Jwts.builder()
                .setSubject(email)
                .claim("id", userId)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + expirationMs))
                .signWith(signingKey(), SignatureAlgorithm.HS256)
                .compact();
    }

    /**
     * Extract email (subject) from token.
     */
    public String extractEmail(String token) {
        return parseClaims(token).getSubject();
    }

    /**
     * Extract user id from token.
     */
    public Long extractUserId(String token) {
        return ((Number) parseClaims(token).get("id")).longValue();
    }

    /**
     * Return true if token is well-formed, signed correctly, and not expired.
     */
    public boolean isValid(String token) {
        try {
            parseClaims(token);
            return true;
        } catch (JwtException | IllegalArgumentException e) {
            return false;
        }
    }

    private Claims parseClaims(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(signingKey())
                .build()
                .parseClaimsJws(token)
                .getBody();
    }
}
