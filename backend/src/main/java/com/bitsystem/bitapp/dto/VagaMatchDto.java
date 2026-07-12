package com.bitsystem.bitapp.dto;

import java.util.List;

public class VagaMatchDto {

    /** Resultado determinístico do cálculo (sem IA) — usado internamente e no lote. */
    public record Resultado(
        Integer matchPercentual,
        List<String> skillsAtendidas,
        List<String> skillsFaltantes
    ) {}

    /** Resposta de GET /api/vagas/{id}/match — resultado + orientação da IA. */
    public record Detalhe(
        Integer matchPercentual,
        List<String> skillsAtendidas,
        List<String> skillsFaltantes,
        String comoResolver
    ) {
        public static Detalhe from(Resultado r, String comoResolver) {
            return new Detalhe(r.matchPercentual(), r.skillsAtendidas(), r.skillsFaltantes(), comoResolver);
        }
    }

    /** Item de GET /api/vagas/match-lote — só o percentual, sem IA, barato para a listagem. */
    public record LoteItem(
        Long vagaId,
        Integer matchPercentual
    ) {}
}
