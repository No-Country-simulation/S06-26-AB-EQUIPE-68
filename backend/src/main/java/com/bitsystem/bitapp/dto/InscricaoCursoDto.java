package com.bitsystem.bitapp.dto;

import com.bitsystem.bitapp.domain.InscricaoCurso;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDateTime;

public class InscricaoCursoDto {

    /** Payload enviado por POST /api/cursos/inscrever (front manda {cursoId, usuarioId}). */
    public record Request(
        @NotNull Long cursoId,
        Long usuarioId
    ) {}

    public record Response(
        Long id,
        Long usuarioId,
        Long cursoId,
        LocalDateTime dataInscricao,
        String status,
        boolean jaInscrito
    ) {
        public static Response from(InscricaoCurso i, boolean jaInscrito) {
            return new Response(
                i.getId(),
                i.getUsuarioId(),
                i.getCursoId(),
                i.getDataInscricao(),
                i.getStatus(),
                jaInscrito
            );
        }
    }
}