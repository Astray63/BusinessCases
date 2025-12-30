package com.eb.electricitybusiness.security;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

import java.security.Key;
import java.util.Date;

/**
 * Classe utilitaire pour les opérations sur les tokens JWT.
 * Gère la génération, validation et analyse des tokens.
 */
@Component
public class JwtUtils {
    private static final Logger logger = LoggerFactory.getLogger(JwtUtils.class);

    private final Key jwtSecret;
    private final long jwtExpirationMs;
    private final long jwtRefreshExpirationMs;
    private final SignatureAlgorithm algorithm;

    public JwtUtils(
            @Value("${app.jwt.secret}") String secretKey,
            @Value("${app.jwt.expiration-ms}") long expirationMs,
            @Value("${app.jwt.refresh-expiration-ms}") long refreshExpirationMs,
            @Value("${app.jwt.algorithm:HS256}") String algorithm) {
        this.jwtSecret = Keys.hmacShaKeyFor(secretKey.getBytes());
        this.jwtExpirationMs = expirationMs;
        this.jwtRefreshExpirationMs = refreshExpirationMs;
        this.algorithm = SignatureAlgorithm.forName(algorithm);
    }

    /**
     * Génère un token JWT pour l'utilisateur authentifié
     * 
     * @param authentication Objet d'authentification Spring Security
     * @return Token JWT généré
     */
    public String generateJwtToken(Authentication authentication) {
        String username = authentication.getName();

        return Jwts.builder()
                .setSubject(username)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + jwtExpirationMs))
                .signWith(jwtSecret, algorithm)
                .compact();
    }

    /**
     * Génère un refresh token
     * 
     * @param username Identifiant utilisateur
     * @return Refresh token généré
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
     * Extrait le nom d'utilisateur du token JWT
     * 
     * @param token Token JWT
     * @return Nom d'utilisateur issu du token
     * @throws JwtException si le token est invalide
     */
    public String getUsernameFromToken(String token) throws JwtException {
        return parseToken(token).getSubject();
    }

    /**
     * Valide le token JWT
     * 
     * @param token Token JWT à valider
     * @return vrai si le token est valide
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
