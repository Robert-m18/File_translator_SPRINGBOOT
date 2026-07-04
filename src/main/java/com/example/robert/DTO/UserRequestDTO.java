package com.example.robert.DTO;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record UserRequestDTO(

        @NotBlank(message = "Imię nie może być puste")
        String name,

        @Email(message = "Niepoprawny format email")
        @NotBlank(message = "Email nie może być pusty")
        String email,

        @NotBlank(message = "Hasło nie może być puste")
        String password

) {}
