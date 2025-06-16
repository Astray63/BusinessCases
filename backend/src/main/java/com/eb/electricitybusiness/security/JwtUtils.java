package com.eb.electricitybusiness.security;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import io.jsonwebtoken.security.SignatureException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Component;

import java.security.Key;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class JwtUtils {
    private final Key jwtSecret;
    private final int jwtExpirationMs;

    public JwtUtils(
        @Value("${app.jwt.secret}") String secretKey,
        @Value("${app.jwt.expiration-ms}") int expirationMs
    ) {
        this.jwtSecret = Keys.hmacShaKeyFor(secretKey.getBytes());
        this.jwtExpirationMs = expirationMs;
    }

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
            .signWith(jwtSecret, SignatureAlgorithm.HS256)
            .compact();
    }

    public String getUsernameFromToken(String token) {
        return Jwts.parserBuilder()
            .setSigningKey(jwtSecret)
            .build()
            .parseClaimsJws(token)
            .getBody()
            .getSubject();
    }

    public boolean validateToken(String token) {
        try {
            Jwts.parserBuilder()
                .setSigningKey(jwtSecret)
                .build()
                .parseClaimsJws(token);
            return true;
        } catch (SignatureException e) {
            // Log invalid JWT signature
        } catch (MalformedJwtException e) {
            // Log invalid JWT token
        } catch (ExpiredJwtException e) {
            // Log expired JWT token
        } catch (UnsupportedJwtException e) {
            // Log unsupported JWT token
        } catch (IllegalArgumentException e) {
            // Log empty claims
        }
        return false;
    }
}
