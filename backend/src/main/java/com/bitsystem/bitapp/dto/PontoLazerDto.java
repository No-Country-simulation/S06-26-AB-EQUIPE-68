package com.bitsystem.bitapp.dto;

import java.util.List;

/**
 * ============================================================================
 * CLASSES DTO: PontoLazerDto
 * ============================================================================
 *
 * Ponto de interesse de Lazer & Cultura, servido por GET /api/lazer/pontos.
 * Antes hardcoded no frontend (frontend/js/lazer.js); migrado para o backend
 * como fonte única, agora enriquecido com o selo de zona de movimento
 * (Vísent-c).
 *
 * @author BiT System
 * @version 1.0.0
 */
public class PontoLazerDto {

    /** Dado semente de um ponto (sem zona — calculada à parte por LazerService). */
    public record Seed(
        int id,
        String nome,
        String tipo,
        String regiao,
        double lat,
        double lng,
        String descricao,
        boolean gratuito,
        boolean acessivel,
        String horario,
        List<String> tags
    ) {}

    /**
     * RESPOSTA: ponto de Lazer com a zona de movimento já calculada.
     */
    public record Response(
        int id,
        String nome,
        String tipo,
        String regiao,
        double lat,
        double lng,
        String descricao,
        boolean gratuito,
        boolean acessivel,
        String horario,
        List<String> tags,

        /** Selo de concentração de movimento perto do ponto (Vísent-c):
         *  "tranquila" | "moderada" | "movimentada". */
        String zonaMovimento
    ) {}
}