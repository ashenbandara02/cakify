package com.cakify.security;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Component;

import java.security.Key;
import java.util.Date;

@Component
public class JwtUtil {

    // Must be at least 32 bytes (this one is 64)
    private static final String SECRET_KEY = "b12f60e8b5a9a3d84f49d1cb23e4d8a7b7e9f8d6c5a4b3c2d1e0f9a8b7c6d5e4";

    private static final long EXPIRATION_TIME = 24 * 60 * 60 * 1000; // 24h

    private Key getSigningKey() {
        // Use Keys.hmacShaKeyFor safely each time to avoid initialization issues
        return Keys.hmacShaKeyFor(SECRET_KEY.getBytes());
    }

    // Generate JWT token
    public String generateToken(String username) {
        return Jwts.builder()
                .setSubject(username)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + EXPIRATION_TIME))
                .signWith(getSigningKey(), SignatureAlgorithm.HS256)
                .compact();
    }

    // Validate token
    public boolean validateToken(String token) {
        try {
            Jwts.parserBuilder()
                    .setSigningKey(getSigningKey())
                    .build()
                    .parseClaimsJws(token);
            return true;
        } catch (JwtException | IllegalArgumentException e) {
            System.err.println("Invalid JWT: " + e.getMessage());
            return false;
        }
    }

    // Extract username
    public String extractUsername(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(getSigningKey())
                .build()
                .parseClaimsJws(token)
                .getBody()
                .getSubject();
    }
}
