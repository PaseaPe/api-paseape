package com.paseape.apipaseape.infrastructure.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.UnsupportedJwtException;
import io.jsonwebtoken.security.Keys;
import io.jsonwebtoken.security.SecurityException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

import static com.paseape.apipaseape.infrastructure.constant.Constant.*;

@Component
public class JwtTokenProvider {

    private static final Logger log = LoggerFactory.getLogger(JwtTokenProvider.class);

    private final SecretKey key;
    private final long expirationMs;

    public JwtTokenProvider(
            @Value("${app.security.jwt-secret}") String secret,
            @Value("${app.security.jwt-expiration-ms:86400000}") long expirationMs) {
        this.key = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
        this.expirationMs = expirationMs;
    }

    public String generateToken(Long userId, String email, String role) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + expirationMs);

        return Jwts.builder()
                .subject(String.valueOf(userId))
                .claim("email", email != null ? email.trim().toLowerCase() : "")
                .claim("role", role != null ? role.toUpperCase() : "OWNER")
                .issuedAt(now)
                .expiration(expiryDate)
                .signWith(key)
                .compact();
    }

    public String generatePasswordResetToken(Long userId, String email) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + EXPIRATION_RESET_TOKEN_MS);
        return Jwts.builder()
                .subject(String.valueOf(userId))
                .claim("email", email != null ? email.trim().toLowerCase() : "")
                .claim(PURPOSE_CLAIM, PURPOSE_PASSWORD_RESET)
                .issuedAt(now)
                .expiration(expiryDate)
                .signWith(key)
                .compact();
    }

    public boolean validatePasswordResetToken(String token) {
        try {
            Claims claims = Jwts.parser().verifyWith(key).build().parseSignedClaims(token).getPayload();
            String purpose = claims.get(PURPOSE_CLAIM, String.class);
            return PURPOSE_PASSWORD_RESET.equals(purpose);
        } catch (JwtException | IllegalArgumentException e) {
            log.warn("[JWT-RESET] Token de restablecimiento invalido: {}", e.getMessage());
            return false;
        }
    }

    public boolean validateToken(String token) {
        try {
            Jwts.parser().verifyWith(key).build().parseSignedClaims(token);
            return true;
        } catch (SecurityException | MalformedJwtException e) {
            log.warn("[JWT-AUTH] Firma criptográfica inválida o token corrupto: {}", e.getMessage());
        } catch (ExpiredJwtException e) {
            log.warn("[JWT-AUTH] Token expirado: {}", e.getMessage());
        } catch (UnsupportedJwtException e) {
            log.warn("[JWT-AUTH] Formato de token no soportado: {}", e.getMessage());
        } catch (IllegalArgumentException e) {
            log.warn("[JWT-AUTH] Cadena de claims vacía o nula: {}", e.getMessage());
        } catch (JwtException e) {
            log.warn("[JWT-AUTH] Error general de procesamiento JWT: {}", e.getMessage());
        }
        return false;
    }

    public Claims getClaims(String token) {
        return Jwts.parser()
                .verifyWith(key)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    public String getEmailFromToken(String token) {
        Claims claims = getClaims(token);
        String email = claims.get("email", String.class);
        return email != null ? email : claims.getSubject();
    }

    public String getRoleFromToken(String token) {
        return getClaims(token).get("role", String.class);
    }

    public Long getUserIdFromToken(String token) {
        return Long.parseLong(getClaims(token).getSubject());
    }
}