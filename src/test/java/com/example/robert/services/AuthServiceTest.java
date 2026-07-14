package com.example.robert.services;

import com.example.robert.DTO.UserRequestDTO;
import com.example.robert.config.JWT.JwtUtil;
import com.example.robert.exceptions.EmailAlreadyExistException;
import com.example.robert.exceptions.JwtAuthenticationException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private JwtUtil jwtUtil;

    @Mock
    private UserServiceInterface userService;

    @InjectMocks
    private AuthService authService;


    @Test
    void register_shouldSaveUser_whenEmailNotExists() {
        // given
        UserRequestDTO dto = new UserRequestDTO("Adrian", "adrian@test.pl", "haslo123");
        when(userService.userExistsByEmail(dto.email())).thenReturn(false);
        when(jwtUtil.generateToken(dto.email())).thenReturn("mocked-jwt-token");
        when(jwtUtil.generateRefreshToken(dto.email())).thenReturn("mocked-refresh-token");
        //save user zbędne - metoda jest void, więc mock ją zignoruje i nie wywwoła zapisu w bazie

        // when
        authService.register(dto);

        // then
        verify(userService, times(1)).saveUser(dto);
        verify(jwtUtil, times(1)).generateToken(dto.email());
        verify(jwtUtil, times(1)).generateRefreshToken(dto.email());
    }

    @Test
    void register_shouldThrowException_whenEmailAlreadyExists() {
        // given
        UserRequestDTO dto = new UserRequestDTO("Adrian", "adrian@test.pl", "haslo123");
        when(userService.userExistsByEmail(dto.email())).thenReturn(true);

        // when / then
        assertThrows(EmailAlreadyExistException.class, () -> authService.register(dto));
         verify(userService, never()).saveUser(any());
    }

    @Test
    void login_shouldReturnTokenPair() {
        // given
        UserRequestDTO dto = new UserRequestDTO("Adrian", "adrian@test.pl", "haslo123");
        when(authenticationManager.authenticate(any())).thenReturn(new UsernamePasswordAuthenticationToken(dto.email(), dto.password()));
        when(jwtUtil.generateToken(any())).thenReturn("mocked-jwt-token");

        // when
        var result = authService.login(dto.email(), dto.password());

        // then
        assertEquals("mocked-jwt-token", result.accessToken());
    }

    @Test
    void login_shouldThrowException_whenCredentialsInvalid() {
        when(authenticationManager.authenticate(any()))
                .thenThrow(new BadCredentialsException("Złe dane logowania"));

        assertThrows(BadCredentialsException.class,
                () -> authService.login("adrian@test.pl", "złehaslo"));
    }

    @Test
    void refreshToken_shouldReturnNewTokenPair() {
        // given
        String refreshToken = "mocked-refresh-token";
        when(jwtUtil.extractTokenType(refreshToken)).thenReturn("refresh");
        when(jwtUtil.generateToken(any())).thenReturn("new-access-token");
        when(jwtUtil.generateRefreshToken(any())).thenReturn("new-refresh-token");
        // when
        var result = authService.refreshToken(refreshToken);
        // then
        assertEquals("new-access-token", result.accessToken());
        assertEquals("new-refresh-token", result.refreshToken());
    }

    @Test
    void refreshToken_shouldThrowException_whenInvalidTokenType() {
        // given
        String refreshToken = "mocked-refresh-token";
        when(jwtUtil.extractTokenType(refreshToken)).thenReturn("access");
        // when / then
       String result =  assertThrows(JwtAuthenticationException.class, () -> authService.refreshToken(refreshToken)).getTokenError();
        verify(jwtUtil, never()).generateToken(any());
        verify(jwtUtil, never()).generateRefreshToken(any());
        assertEquals("INVALID_TOKEN_TYPE", result);
    }
}
