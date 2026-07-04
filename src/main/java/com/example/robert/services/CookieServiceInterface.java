package com.example.robert.services;

import org.springframework.http.ResponseCookie;

public interface CookieServiceInterface {
   public ResponseCookie createAccessTokenCookie(String token);
   public ResponseCookie createRefreshTokenCookie(String token);
   public ResponseCookie clearRefreshTokenCookie();
   public ResponseCookie clearAccessTokenCookie();
}
