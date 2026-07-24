package com.bitsystem.bitapp.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDateTime;

public class SolicitacaoMentoriaDto {

    /** Payload enviado por POST /api/mentorias. usuarioId vem por RequestParam (mesmo padrão de AssessmentController). */
    public record Request(
        @NotNull Long mentorId,
        @NotBlank String areaSolicitada,
        String mensagem
    ) {}

    public record Response(
        Long id,
        Long usuarioId,
        Long mentorId,
        String mentorNome,
        String areaSolicitada,
        String mensagem,
        String status,
        LocalDateTime createdAt
    ) {}

    /**
     * RESPOSTA: Item do histórico de solicitações de mentoria
     *
     * Retornado por GET /api/mentorias/historico, mais recente primeiro.
     */
    public record HistoricoResponse(
        Long id,
        Long mentorId,
        String mentorNome,
        String areaSolicitada,
        String mensagem,
        String status,
        LocalDateTime createdAt
    ) {}

    /** Payload enviado por PATCH /api/mentorias/{id}/status. */
    public record AtualizarStatusRequest(
        @NotBlank String status
    ) {}
}
