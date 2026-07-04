package com.example.robert.controllers;


import com.example.robert.DTO.LoginRequest;
import com.example.robert.DTO.TokenPair;
import com.example.robert.DTO.UserRequestDTO;
import com.example.robert.responseModels.SuccessMessage;
import com.example.robert.services.AuthServiceInterface;
import com.example.robert.services.CookieServiceInterface;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.time.LocalDateTime;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthServiceInterface authService;
    private final CookieServiceInterface cookieService;




    @PostMapping("/login")
    public ResponseEntity<SuccessMessage> login(@Valid @RequestBody LoginRequest request,
                                              HttpServletResponse response) {
        TokenPair tokens = authService.login(request.email(), request.password());

        response.addHeader(HttpHeaders.SET_COOKIE,
                cookieService.createAccessTokenCookie(tokens.accessToken()).toString());
        response.addHeader(HttpHeaders.SET_COOKIE,
                cookieService.createRefreshTokenCookie(tokens.refreshToken()).toString());


        return ResponseEntity.status(HttpStatus.OK)
                .body(new SuccessMessage("Zalogowano pomyślnie", LocalDateTime.now()));
    }

    @PostMapping("/register")
    public ResponseEntity<SuccessMessage> register(
            @Valid @RequestBody UserRequestDTO request,
            HttpServletResponse response) {

        TokenPair tokens = authService.register(request);

        response.addHeader(HttpHeaders.SET_COOKIE,
                cookieService.createAccessTokenCookie(tokens.accessToken()).toString());
        response.addHeader(HttpHeaders.SET_COOKIE,
                cookieService.createRefreshTokenCookie(tokens.refreshToken()).toString());


        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new SuccessMessage("Zarejestrowano pomyślnie", LocalDateTime.now()));
    }

    @PostMapping("/refresh")
    public ResponseEntity<SuccessMessage> refresh(
            @CookieValue(name = "refreshToken", required = false) String refreshToken,
            HttpServletResponse response) {

        if (refreshToken == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        TokenPair newTokens = authService.refreshToken(refreshToken);

        response.addHeader(HttpHeaders.SET_COOKIE,
                cookieService.createAccessTokenCookie(newTokens.accessToken()).toString());
        response.addHeader(HttpHeaders.SET_COOKIE,
                cookieService.createRefreshTokenCookie(newTokens.refreshToken()).toString());

        return ResponseEntity.ok(new SuccessMessage("Token odświeżony", LocalDateTime.now()));
    }

    @PostMapping("/logout")
    public ResponseEntity<SuccessMessage> logout(HttpServletResponse response) {
        response.addHeader(HttpHeaders.SET_COOKIE,
                cookieService.clearAccessTokenCookie().toString());
        response.addHeader(HttpHeaders.SET_COOKIE,
                cookieService.clearRefreshTokenCookie().toString());

        return ResponseEntity.ok(new SuccessMessage("Wylogowano pomyślnie", LocalDateTime.now()));
    }

}
