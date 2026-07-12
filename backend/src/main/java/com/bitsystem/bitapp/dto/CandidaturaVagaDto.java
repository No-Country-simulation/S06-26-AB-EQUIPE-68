package com.bitsystem.bitapp.dto;

import com.bitsystem.bitapp.domain.CandidaturaVaga;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDateTime;

public class CandidaturaVagaDto {

    /** Payload enviado por POST /api/vagas/enviar-curriculo (front manda {vagaId, usuarioId}). */
    public record Request(
        @NotNull Long vagaId,
        Long usuarioId,
        String nome,
        String email,
        String mensagem
    ) {}

    public record Response(
        Long id,
        Long usuarioId,
        Long vagaId,
        String nome,
        String email,
        LocalDateTime dataEnvio,
        String status
    ) {
        public static Response from(CandidaturaVaga c) {
            return new Response(
                c.getId(),
                c.getUsuarioId(),
                c.getVagaId(),
                c.getNome(),
                c.getEmail(),
                c.getDataEnvio(),
                c.getStatus()
            );
        }
    }
}