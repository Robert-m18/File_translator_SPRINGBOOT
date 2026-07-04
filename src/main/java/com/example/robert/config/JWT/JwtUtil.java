package com.example.robert.config.JWT;

import com.example.robert.exceptions.JwtAuthenticationException;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Date;

/**
 * Utility do obsługi JWT tokenów
 * Obsługuje: generowanie, walidację i ekstrakcję danych z tokenów
 */
@Slf4j
@Component
public class JwtUtil {

    @Value("${jwt.secret}")
    private String secret;

    @Value("${jwt.expiration}")
    private long expiration;

    @Value("${jwt.refresh-expiration}")
    private long refreshExpiration;

    /**
     * Zamienia String na kryptograficzny klucz
     */
    private SecretKey getKey() {
        return Keys.hmacShaKeyFor(Decoders.BASE64.decode(secret));
    }

    /**
     * Generuje JWT token dla podanego użytkownika
     */
    public String generateToken(String username) {
        log.info("Generowanie JWT tokena dla użytkownika: {}", username);
        return Jwts.builder()
                .claim("type", "access")
                .subject(username)
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + expiration))
                .signWith(getKey())
                .compact();
    }


    public String generateRefreshToken(String username) {
        return Jwts.builder()
                .claim("type", "refresh")
                .subject(username)
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + refreshExpiration))
                .signWith(getKey())
                .compact();
    }

    /**
     * Wyciąga nazwę użytkownika z tokenu
     */
    public String extractUsername(String token) {
        try {
            return extractClaims(token).getSubject();
        } catch (ExpiredJwtException ex) {
            log.warn("Token JWT wygasł");
            throw new JwtAuthenticationException("Token wygasł", "EXPIRED_TOKEN", ex);
        } catch (JwtException ex) {
            log.warn("Nieprawidłowy token JWT: {}", ex.getMessage());
            throw new JwtAuthenticationException("Nieprawidłowy token", "INVALID_TOKEN", ex);
        }
    }

    /**
     * Sprawdza czy token jest jeszcze ważny (nie wygasł)
     */
    public boolean isTokenValid(String token) {
        try {
            Date expiration = extractClaims(token).getExpiration();
            boolean isValid = expiration.after(new Date());
            log.debug("Token JWT walidacja: {}", isValid ? "OK" : "WYGASŁ");
            return isValid;
        } catch (ExpiredJwtException ex) {
            log.warn("Token JWT wygasł");
            throw new JwtAuthenticationException("Token wygasł", "EXPIRED_TOKEN", ex);
        } catch (JwtException ex) {
            log.warn("Nieprawidłowy token JWT: {}", ex.getMessage());
            throw new JwtAuthenticationException("Nieprawidłowy token", "INVALID_TOKEN", ex);
        } catch (Exception ex) {
            log.error("Błąd przy walidacji tokena: ", ex);
            throw new JwtAuthenticationException("Błąd przy walidacji", "VALIDATION_ERROR", ex);
        }
    }

    /**
     * Pomocnicza - parsuje token i zwraca payload
     */
    private Claims extractClaims(String token) {
        return Jwts.parser()
                .verifyWith(getKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    public String extractTokenType(String token) {
        return (String) extractClaims(token).get("type");
    }
}
