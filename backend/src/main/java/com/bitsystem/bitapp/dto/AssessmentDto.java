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
        String tipo
    ) {}

    public record Response(
        Integer compatibilidade,
        String nivel,
        List<String> pontosFortes,
        List<String> gaps,
        List<String> planoDesenvolvimento
    ) {}
}
