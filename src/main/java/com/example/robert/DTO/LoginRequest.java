package com.example.robert.DTO;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record LoginRequest(
		@Email(message = "Nieprawidłowy format email")
		@NotBlank(message = "Email nie może być pusty")
		String email,

		@NotBlank(message = "Hasło nie może być puste")
		@Size(min = 8, message = "Hasło musi mieć co najmniej {min} znaków")
		String password
) {}

