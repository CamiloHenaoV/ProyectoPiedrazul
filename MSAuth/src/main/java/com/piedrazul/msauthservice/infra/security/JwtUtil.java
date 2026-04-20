package com.piedrazul.msauthservice.infra.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.Map;

/**
 * Utilidad para generar y validar JWTs.
 * El secret se inyecta desde application.yaml (o variable de entorno).
 */
@Component
public class JwtUtil {

    private final SecretKey key;
    private final long accessExpiration;

    public JwtUtil(
            @Value("${jwt.secret}") String secret,
            @Value("${jwt.access-expiration}") long accessExpiration) {
        this.key = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
        this.accessExpiration = accessExpiration;
    }

    /** Genera un access token con los claims del usuario */
    public String generarAccessToken(Long usuarioId, String login, String rol) {
        return Jwts.builder()
                .subject(String.valueOf(usuarioId))
                .claim("login", login)
                .claim("rol", rol)
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + accessExpiration))
                .signWith(key)
                .compact();
    }

    /** Extrae todos los claims de un token (lanza excepción si es inválido o expirado) */
    public Claims extraerClaims(String token) {
        return Jwts.parser()
                .verifyWith(key)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    public Long extraerUsuarioId(String token) {
        return Long.valueOf(extraerClaims(token).getSubject());
    }

    public String extraerLogin(String token) {
        return extraerClaims(token).get("login", String.class);
    }

    public String extraerRol(String token) {
        return extraerClaims(token).get("rol", String.class);
    }

    public boolean esValido(String token) {
        try {
            extraerClaims(token);
            return true;
        } catch (Exception e) {
            return false;
        }
    }
}
