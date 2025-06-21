package com.eb.electricitybusiness.security;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Component;

import java.security.Key;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Utility class for JWT token operations.
 * Handles token generation, validation and parsing.
 */
@Component
public class JwtUtils {
    private static final Logger logger = LoggerFactory.getLogger(JwtUtils.class);
    
    private final Key jwtSecret;
    private final int jwtExpirationMs;
    private final int jwtRefreshExpirationMs;
    private final SignatureAlgorithm algorithm;

    public JwtUtils(
        @Value("${app.jwt.secret}") String secretKey,
        @Value("${app.jwt.expiration-ms}") int expirationMs,
        @Value("${app.jwt.refresh-expiration-ms}") int refreshExpirationMs,
        @Value("${app.jwt.algorithm:HS256}") String algorithm
    ) {
        this.jwtSecret = Keys.hmacShaKeyFor(secretKey.getBytes());
        this.jwtExpirationMs = expirationMs;
        this.jwtRefreshExpirationMs = refreshExpirationMs;
        this.algorithm = SignatureAlgorithm.forName(algorithm);
    }

    /**
     * Generates a JWT token for authenticated user
     * @param authentication Spring Security authentication object
     * @return Generated JWT token
     */
    public String generateJwtToken(Authentication authentication) {
        String username = authentication.getName();
        List<String> roles = authentication.getAuthorities().stream()
            .map(GrantedAuthority::getAuthority)
            .collect(Collectors.toList());

        return Jwts.builder()
            .setSubject(username)
            .claim("roles", roles)
            .setIssuedAt(new Date())
            .setExpiration(new Date(System.currentTimeMillis() + jwtExpirationMs))
            .signWith(jwtSecret, algorithm)
            .compact();
    }

    /**
     * Generates a refresh token
     * @param username User identifier
     * @return Generated refresh token
     */
    public String generateRefreshToken(String username) {
        return buildToken(username, jwtRefreshExpirationMs);
    }

    private String buildToken(String subject, long expirationMs) {
        return Jwts.builder()
            .setSubject(subject)
            .setIssuedAt(new Date())
            .setExpiration(new Date(System.currentTimeMillis() + expirationMs))
            .signWith(jwtSecret, algorithm)
            .compact();
    }

    /**
     * Extracts username from JWT token
     * @param token JWT token
     * @return Username from token
     * @throws JwtException if token is invalid
     */
    public String getUsernameFromToken(String token) throws JwtException {
        return parseToken(token).getSubject();
    }

    /**
     * Validates JWT token
     * @param token JWT token to validate
     * @return true if token is valid
     */
    public boolean validateToken(String token) {
        try {
            parseToken(token);
            return true;
        } catch (JwtException | IllegalArgumentException e) {
            logger.error("Invalid JWT token: {}", e.getMessage());
            return false;
        }
    }

    private Claims parseToken(String token) throws JwtException {
        return Jwts.parserBuilder()
            .setSigningKey(jwtSecret)
            .build()
            .parseClaimsJws(token)
            .getBody();
    }
}
