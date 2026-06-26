package com.bitsystem.bitapp.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.time.LocalDateTime;

public class AuthDto {

    public record RegisterRequest(
        @NotBlank String nome,
        @NotBlank @Email String email,
        @NotBlank @Size(min = 6) String password,
        String cidade,
        String whatsapp,
        String nivelProfissional,
        String areaTecnologia,
        String competenciasAtuais
    ) {}

    public record LoginRequest(
        @NotBlank @Email String email,
        @NotBlank String password
    ) {}

    public record ProfileUpdateRequest(
        String nome,
        String cidade,
        String whatsapp,
        String nivelProfissional,
        String areaTecnologia,
        String competenciasAtuais
    ) {}

    public record Response(
        String token,
        String refreshToken,
        Long userId,
        String nome,
        String email,
        String cidade,
        String whatsapp,
        String nivelProfissional,
        String areaTecnologia,
        String competenciasAtuais
    ) {}

    public record UserResponse(
        Long id,
        String nome,
        String email,
        String cidade,
        String whatsapp,
        String nivelProfissional,
        String areaTecnologia,
        String competenciasAtuais,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
    ) {}
}
