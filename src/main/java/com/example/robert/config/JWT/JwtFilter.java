package com.example.robert.config.JWT;

import com.example.robert.exceptions.JwtAuthenticationException;
import com.example.robert.responseModels.ErrorMessage;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.time.LocalDateTime;

/**
 * JWT Filter - waliduje JWT token z każdego żądania HTTP
 * Token powinien być umieszczony w ciasteczku "accessToken"
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class JwtFilter extends OncePerRequestFilter {

    private final JwtUtil jwtUtil;
    private final UserDetailsService userDetailsService;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain)
            throws ServletException, IOException {

        // 1. Szukamy tokena w cookie
        String token = extractTokenFromCookies(request);

        if (token == null) {
            filterChain.doFilter(request, response);
            return;
        }

        try {
            // 2. Parsujemy token — rzuca wyjątek jeśli nieważny/wygasły
            String username = jwtUtil.extractUsername(token);

            // 3. Ustawiamy autentykację tylko jeśli jeszcze nie ustawiona
            if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {
                UserDetails userDetails = userDetailsService.loadUserByUsername(username);

                log.debug("Token JWT zweryfikowany dla użytkownika: {}", username);

                UsernamePasswordAuthenticationToken authToken =
                        new UsernamePasswordAuthenticationToken(
                                userDetails,
                                null,
                                userDetails.getAuthorities()
                        );

                SecurityContextHolder.getContext().setAuthentication(authToken);
            }

            filterChain.doFilter(request, response);

        } catch (JwtAuthenticationException ex) {
            log.warn("JWT błąd: {}", ex.getMessage());
            sendUnauthorized(response, ex.getMessage());
        } catch (Exception ex) {
            log.error("Błąd w JWT filtrze: ", ex);
            sendUnauthorized(response, "Błąd przy przetwarzaniu tokena");
        }
    }

    // Wydzielone do osobnej metody dla czytelności
    private String extractTokenFromCookies(HttpServletRequest request) {
        if (request.getCookies() == null) return null;

        for (Cookie cookie : request.getCookies()) {
            if ("accessToken".equals(cookie.getName())) {
                return cookie.getValue();
            }
        }
        return null;
    }

    private void sendUnauthorized(HttpServletResponse response, String message) throws IOException {
        ErrorMessage error = new ErrorMessage(message, 401, LocalDateTime.now());

        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType("application/json;charset=UTF-8");
        response.getWriter().write(
                String.format(
                        "{\"message\":\"%s\",\"status\":%d,\"timestamp\":\"%s\"}",
                        error.message(), error.status(), error.timestamp()
                )
        );
    }
}