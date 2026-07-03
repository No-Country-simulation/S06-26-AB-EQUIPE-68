package com.bitsystem.bitapp.dto;

/**
 * Dica de lazer/bem-estar candidata (seed do SugestaoService), migrada de
 * frontend/js/saude-mental.js (DICAS_LAZER/DICAS_GERAIS).
 *
 * @param categoria taxonomia fixa usada pelo fallback determinístico:
 *        natureza | esporte | cultura | social | calma | pausa | estudo
 * @param offlineFriendly false só para as dicas que dependem de
 *        streaming/ligação (o restante são atividades físicas presenciais)
 */
public record DicaLazerDto(
    String icon,
    String titulo,
    String desc,
    String categoria,
    boolean offlineFriendly
) {}