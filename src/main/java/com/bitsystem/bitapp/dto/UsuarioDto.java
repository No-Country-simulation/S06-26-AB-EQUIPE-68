package com.bitsystem.bitapp.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

/**
 * ============================================================================
 * CLASSES DTO: UsuarioDto
 * ============================================================================
 *
 * Data Transfer Objects para o endpoint de cadastro/atualização de usuário.
 * Substitui o formulário Thymeleaf (POST /onboarding) por um endpoint REST
 * puro (POST /api/usuarios) que retorna JSON com o usuário criado/atualizado.
 *
 * FLUXO:
 * Client → POST /api/usuarios + JSON Request → Service → DB → Response JSON
 *
 * @author BiT System
 * @version 1.0.0
 */
public class UsuarioDto {

    /**
     * REQUISIÇÃO: Dados do formulário de onboarding enviados pelo frontend
     */
    public record Request(
        @NotBlank(message = "Nome é obrigatório")
        String nome,

        @NotBlank(message = "Email é obrigatório")
        @Email(message = "Email inválido")
        String email,

        String cidade,
        String whatsapp,
        String nivelProfissional,
        String areaTecnologia,
        String competenciasAtuais
    ) {}

    /**
     * RESPOSTA: Dados do usuário salvo retornados para o frontend.
     * O frontend armazena esses dados no localStorage para uso nas demais
     * chamadas da API (orientar, saude, network-status).
     */
    public record Response(
        Long id,
        String nome,
        String email,
        String cidade,
        String whatsapp,
        String nivelProfissional,
        String areaTecnologia,
        String competenciasAtuais
    ) {}
}
