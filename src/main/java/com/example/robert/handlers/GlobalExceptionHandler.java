package com.example.robert.handlers;

import com.example.robert.responseModels.ErrorMessage;
import com.example.robert.exceptions.EmailAlreadyExistException;
import com.example.robert.exceptions.JwtAuthenticationException;
import com.example.robert.exceptions.NotFoundException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import java.time.LocalDateTime;
import java.util.stream.Collectors;

/**
 * Centralny handler dla wszystkich wyjątków w aplikacji
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(NotFoundException.class)
    public ResponseEntity<ErrorMessage> handleNotFoundException(NotFoundException ex) {
        log.warn("Zasób nie znaleziony: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(new ErrorMessage(ex.getMessage(), HttpStatus.NOT_FOUND.value(), LocalDateTime.now()));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorMessage> handleValidationException(MethodArgumentNotValidException ex) {
        String message = ex.getBindingResult()
                .getFieldErrors()
                .stream()
                .map(error -> error.getField() + ": " + error.getDefaultMessage())
                .collect(Collectors.joining(", "));

        log.warn("Błąd walidacji: {}", message);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(new ErrorMessage(message, HttpStatus.BAD_REQUEST.value(), LocalDateTime.now()));
    }

    @ExceptionHandler(EmailAlreadyExistException.class)
    public ResponseEntity<ErrorMessage> handleEmailExists(EmailAlreadyExistException ex) {
        log.warn("Email już istnieje: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(new ErrorMessage(ex.getMessage(), HttpStatus.CONFLICT.value(), LocalDateTime.now()));
    }

    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<ErrorMessage> handleBadCredentials(BadCredentialsException ex) {
        log.warn("Nieprawidłowe dane logowania");
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(new ErrorMessage("Nieprawidłowy email lub hasło", HttpStatus.UNAUTHORIZED.value(), LocalDateTime.now()));
    }

    /**
     * Obsługuje błędy JWT - invalid token, expired token, etc.
     */
    @ExceptionHandler(JwtAuthenticationException.class)
    public ResponseEntity<ErrorMessage> handleJwtAuthenticationException(JwtAuthenticationException ex) {
        log.warn("Błąd JWT autentykacji: {} - {}", ex.getTokenError(), ex.getMessage());
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(new ErrorMessage("Błąd autentykacji: " + ex.getMessage(), HttpStatus.UNAUTHORIZED.value(), LocalDateTime.now()));
    }

    /**
     * Fallback dla nieobsługiwanych wyjątków
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorMessage> handleGenericException(Exception ex) {
        log.error("Nieoczekiwany błąd: ", ex);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ErrorMessage("Wewnętrzny błąd serwera", HttpStatus.INTERNAL_SERVER_ERROR.value(), LocalDateTime.now()));
    }
}
