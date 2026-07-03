package com.bitsystem.bitapp.dto;

import java.util.List;

/**
 * ============================================================================
 * CLASSES DTO: SugestaoDto
 * ============================================================================
 *
 * Sugestões de lazer/bem-estar personalizadas, retornadas por
 * GET /api/sugestoes/{usuarioId}. Fonte: humor mais recente do histórico de
 * saúde mental + região do usuário + candidatas de DicasLazerSeed, escolhidas
 * por IA (Gemini) com fallback determinístico obrigatório.
 *
 * @author BiT System
 * @version 1.0.0
 */
public class SugestaoDto {

    public record Item(
        String titulo,
        String motivo
    ) {}

    public record Response(
        List<Item> sugestoes
    ) {}
}