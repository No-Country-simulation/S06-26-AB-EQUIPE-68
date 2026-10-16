package com.bitsystem.bitapp.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

public class MentalHealthDto {

    /**
     * Resposta do Mental Health Agent (n8n, webhook bit/agent com tipo "mental-health"):
     * um objeto {mensagem, acaoSugerida}. Se o Gemini do workflow devolver algo que não
     * seja JSON, o n8n responde {error: "PARSE_FALHOU", raw: ...}; aí os dois campos vêm
     * nulos e o SaudeMentalService cai no acolhimento local. Exemplos em
     * src/test/resources/contracts/n8n/.
     */
    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Response(
        String mensagem,
        String acaoSugerida
    ) {}
}
