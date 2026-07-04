package com.bitsystem.bitapp.service;

import static org.junit.jupiter.api.Assertions.*;

import com.bitsystem.bitapp.dto.SaudeDto;
import org.junit.jupiter.api.Test;

class EmotionResponseProviderTest {

    private final EmotionResponseProvider provider = new EmotionResponseProvider();

    @Test
    void idiomaEsDevolveRespostaEmEspanhol() {
        SaudeDto.RawResponse r = provider.resolve("feliz", 8, "es");
        assertTrue(r.mensagem().contains("ligero") || r.mensagem().contains("ligero".toLowerCase()));
        assertFalse(r.mensagem().toLowerCase().contains("leve"), "não deve vazar texto em PT quando idioma=es");
    }

    @Test
    void idiomaPtDevolveRespostaEmPortugues() {
        SaudeDto.RawResponse r = provider.resolve("ansioso", 3, "pt");
        assertTrue(r.mensagem().contains("ansiedade"));
    }

    @Test
    void idiomaAusenteCaiParaPortuguesPorPadrao() {
        SaudeDto.RawResponse comIdiomaNulo = provider.resolve("triste", 5, null);
        SaudeDto.RawResponse comPt = provider.resolve("triste", 5, "pt");
        assertEquals(comPt.mensagem(), comIdiomaNulo.mensagem());
    }

    @Test
    void humorDesconhecidoUsaFallbackPorNotaNoIdiomaCorreto() {
        // nota < 4 sem humor reconhecido -> cai no "ansioso" do idioma pedido
        SaudeDto.RawResponse r = provider.resolve("emoji-nao-mapeado", 2, "es");
        assertTrue(r.mensagem().contains("ansiedad"));
    }

    // ── LOTE 4.1: check-in diário pode chegar sem humor e sem nota ───────────
    @Test
    void semHumorESemNotaNaoLancaExcecao() {
        SaudeDto.RawResponse r = provider.resolve(null, null, "pt");
        assertNotNull(r.mensagem());
        assertFalse(r.mensagem().isBlank());
    }
}
