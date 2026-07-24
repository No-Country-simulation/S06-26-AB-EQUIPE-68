package com.bitsystem.bitapp.dto;

import jakarta.validation.constraints.NotBlank;
import java.util.List;

public class AssessmentDto {

    public record Request(
        @NotBlank String nome,
        // idade opcional: o perfil do Dashboard não coleta idade; o Assessment
        // funciona (e o fallback) com o que existir no perfil, sem novo formulário.
        Integer idade,
        String escolaridade,
        String experiencia,
        List<String> hardSkills,
        List<String> softSkills,
        List<String> tecnologias,
        String tipo,
        /** Idioma da UI ("pt"|"es"), default "pt" — campo aditivo (lote i18n). */
        String idioma,
        /** Área alvo do dropdown de perfil (Java, Web, Dados, ...) — campo aditivo (RAG de roadmaps). */
        String area
    ) {
        public String idiomaOuPadrao() {
            return "es".equalsIgnoreCase(idioma) ? "es" : "pt";
        }
    }

    public record Response(
        Integer compatibilidade,
        String nivel,
        List<String> pontosFortes,
        List<String> gaps,
        List<String> planoDesenvolvimento
    ) {}

    /**
     * RESPOSTA: Item do histórico de assessments de carreira
     *
     * Retornado por GET /api/assessment/historico
     * Lista de avaliações anteriores do usuário, mais recente primeiro.
     */
    public record HistoricoResponse(
        Long id,
        Integer compatibilidade,
        String nivel,
        List<String> pontosFortes,
        List<String> gaps,
        List<String> planoDesenvolvimento,
        java.time.LocalDateTime createdAt
    ) {}
}
