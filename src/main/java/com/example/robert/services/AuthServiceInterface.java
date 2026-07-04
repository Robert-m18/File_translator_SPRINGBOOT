package com.example.robert.services;

import com.example.robert.DTO.TokenPair;
import com.example.robert.DTO.UserRequestDTO;

public interface AuthServiceInterface {
    TokenPair login(String email, String password);
    TokenPair register(UserRequestDTO dto);
    TokenPair refreshToken(String refreshToken);
}

