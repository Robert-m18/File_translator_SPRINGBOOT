package com.example.robert.responseModels;
import java.time.LocalDateTime;

public record ErrorMessage(String message, int status, LocalDateTime timestamp){}
