package com.example.robert.services;

import com.example.robert.DTO.TokenPair;
import com.example.robert.DTO.UserRequestDTO;
import com.example.robert.config.JWT.JwtUtil;
import com.example.robert.exceptions.EmailAlreadyExistException;
import com.example.robert.exceptions.JwtAuthenticationException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Service do obsługi autentykacji - login i rejestracja
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AuthService implements AuthServiceInterface {

    private final AuthenticationManager authenticationManager;
    private final JwtUtil jwtUtil;
    private final UserServiceInterface userService;


    @Override
    public TokenPair login(String email, String password) {
        Authentication auth = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(email, password)
        );

        String accessToken = jwtUtil.generateToken(auth.getName());
        String refreshToken = jwtUtil.generateRefreshToken(auth.getName());

        log.info("Login success email={}", email);
        return new TokenPair(accessToken, refreshToken);
    }

    @Override
    @Transactional
    public TokenPair register(UserRequestDTO dto) {
        log.info("Próba rejestracji użytkownika: {}", dto.email());

        if (userService.userExistsByEmail(dto.email())) {
            log.warn("Email już istnieje w bazie");
            throw new EmailAlreadyExistException("User with this email already exists!");
        }

        userService.saveUser(dto);
        String accessToken = jwtUtil.generateToken(dto.email());
        String refreshToken = jwtUtil.generateRefreshToken(dto.email());
        log.info("Pomyślna rejestracja użytkownika");
        return new TokenPair(accessToken, refreshToken);
    }

    @Override
    public TokenPair refreshToken(String refreshToken) {
        String type = jwtUtil.extractTokenType(refreshToken);
        if (!"refresh".equals(type)) {
            throw new JwtAuthenticationException("Nieprawidłowy typ tokenu", "INVALID_TOKEN_TYPE");
        }
        String username = jwtUtil.extractUsername(refreshToken);
        String accessToken = jwtUtil.generateToken(username);
        String newRefreshToken = jwtUtil.generateRefreshToken(username);
        return new TokenPair(accessToken, newRefreshToken);
    }


}

