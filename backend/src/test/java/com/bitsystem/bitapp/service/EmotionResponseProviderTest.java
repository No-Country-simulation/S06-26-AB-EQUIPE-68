package com.bitsystem.bitapp.service;

import static org.junit.jupiter.api.Assertions.*;

import com.bitsystem.bitapp.dto.SaudeDto;
import org.junit.jupiter.api.Test;

class EmotionResponseProviderTest {

    private final EmotionResponseProvider provider = new EmotionResponseProvider();

    @Test
    void idiomaEsDevolveRespostaEmEspanhol() {
        SaudeDto.RawResponse r = provider.resolve(9, "es");
        assertTrue(r.mensagem().toLowerCase().contains("muy bien"));
        assertFalse(r.mensagem().toLowerCase().contains("leve"), "não deve vazar texto em PT quando idioma=es");
    }

    @Test
    void idiomaPtDevolveRespostaEmPortugues() {
        SaudeDto.RawResponse r = provider.resolve(3, "pt");
        assertTrue(r.mensagem().toLowerCase().contains("tristeza"));
    }

    @Test
    void idiomaAusenteCaiParaPortuguesPorPadrao() {
        SaudeDto.RawResponse comIdiomaNulo = provider.resolve(3, null);
        SaudeDto.RawResponse comPt = provider.resolve(3, "pt");
        assertEquals(comPt.mensagem(), comIdiomaNulo.mensagem());
    }

    @Test
    void notaMuitoTristeRetornaTextoDeReforco() {
        SaudeDto.RawResponse r = provider.resolve(1, "pt");
        assertTrue(r.mensagem().toLowerCase().contains("peso"));
        assertTrue(r.acaoSugerida().contains("188"));
    }

    @Test
    void cadaNivelDaEscalaRetornaRespostaDistinta() {
        var respostas = java.util.List.of(
                provider.resolve(9, "pt").mensagem(),
                provider.resolve(7, "pt").mensagem(),
                provider.resolve(5, "pt").mensagem(),
                provider.resolve(3, "pt").mensagem(),
                provider.resolve(1, "pt").mensagem()
        );
        assertEquals(5, java.util.Set.copyOf(respostas).size(), "os 5 níveis devem ter textos distintos");
    }

    // ── CHECK-IN SÓ-TEXTO: nota null não lança exceção ───────────────────────
    @Test
    void notaAusenteNaoLancaExcecao() {
        SaudeDto.RawResponse r = provider.resolve(null, "pt");
        assertNotNull(r.mensagem());
        assertFalse(r.mensagem().isBlank());
    }

    @Test
    void notaInvalidaLancaExcecao() {
        assertThrows(IllegalArgumentException.class, () -> provider.resolve(4, "pt"));
    }
}
