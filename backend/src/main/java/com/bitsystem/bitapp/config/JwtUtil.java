package com.bitsystem.bitapp.config;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.List;
import java.util.UUID;
import javax.crypto.SecretKey;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class JwtUtil {

    /** Tamanho mínimo da chave do HS256: 256 bits. */
    static final int TAMANHO_MINIMO_BYTES = 32;

    /** Segredos de exemplo já publicados no repositório: nunca valem como segredo real. */
    private static final List<String> SEGREDOS_PUBLICOS = List.of(
            "dev-secret-only-change-in-production-min-512-bits-long-key-here",
            "bitapp-secret-key-change-in-production-512-bits-minimum");

    private final SecretKey secretKey;
    private final long expiration;
    private final long refreshExpiration;

    public JwtUtil(
            @Value("${jwt.secret:}") String secret,
            @Value("${jwt.expiration}") long expiration,
            @Value("${jwt.refresh-expiration}") long refreshExpiration) {
        this.secretKey = Keys.hmacShaKeyFor(validarSegredo(secret).getBytes(StandardCharsets.UTF_8));
        this.expiration = expiration;
        this.refreshExpiration = refreshExpiration;
    }

    /** Falha na inicialização, com mensagem clara, se o segredo faltar ou for fraco. */
    static String validarSegredo(String secret) {
        if (secret == null || secret.isBlank()) {
            throw new IllegalStateException("JWT_SECRET não definido. Defina a variável de ambiente JWT_SECRET "
                    + "com pelo menos " + TAMANHO_MINIMO_BYTES + " caracteres (gere uma com: openssl rand -base64 48).");
        }
        if (secret.getBytes(StandardCharsets.UTF_8).length < TAMANHO_MINIMO_BYTES) {
            throw new IllegalStateException("JWT_SECRET curto demais: o mínimo é " + TAMANHO_MINIMO_BYTES
                    + " bytes (256 bits) para HS256. Gere uma com: openssl rand -base64 48.");
        }
        if (SEGREDOS_PUBLICOS.contains(secret) || secret.startsWith("CHANGE_ME")) {
            throw new IllegalStateException("JWT_SECRET usa um valor de exemplo publicado no repositório. "
                    + "Gere um segredo próprio com: openssl rand -base64 48.");
        }
        return secret;
    }

    public String generateToken(Long userId, String email) {
        Date now = new Date();
        return Jwts.builder()
                .subject(email)
                .claim("userId", userId)
                .id(UUID.randomUUID().toString())
                .issuedAt(now)
                .expiration(new Date(now.getTime() + expiration))
                .signWith(secretKey)
                .compact();
    }

    public String generateRefreshToken(Long userId, String email) {
        Date now = new Date();
        return Jwts.builder()
                .subject(email)
                .claim("userId", userId)
                .claim("type", "refresh")
                .id(UUID.randomUUID().toString())
                .issuedAt(now)
                .expiration(new Date(now.getTime() + refreshExpiration))
                .signWith(secretKey)
                .compact();
    }

    public Claims validateToken(String token) {
        return Jwts.parser()
                .verifyWith(secretKey)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    public Long getUserIdFromToken(String token) {
        return validateToken(token).get("userId", Long.class);
    }

    public String getEmailFromToken(String token) {
        return validateToken(token).getSubject();
    }

    public boolean isTokenExpired(String token) {
        try {
            validateToken(token);
            return false;
        } catch (ExpiredJwtException e) {
            return true;
        }
    }
}
