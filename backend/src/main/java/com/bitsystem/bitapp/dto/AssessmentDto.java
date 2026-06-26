package com.bitsystem.bitapp.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.util.List;

public class AssessmentDto {

    public record Request(
        @NotBlank String nome,
        @NotNull Integer idade,
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
