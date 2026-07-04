package com.example.robert.exceptions;

/**
 * Wyjątek rzucany gdy token JWT jest nieprawidłowy, wygasł lub uszkodzony
 */
public class JwtAuthenticationException extends RuntimeException {

    private final String tokenError;

    public JwtAuthenticationException(String message, String tokenError) {
        super(message);
        this.tokenError = tokenError;
    }

    public JwtAuthenticationException(String message, String tokenError, Throwable cause) {
        super(message, cause);
        this.tokenError = tokenError;
    }

    public String getTokenError() {
        return tokenError;
    }
}

