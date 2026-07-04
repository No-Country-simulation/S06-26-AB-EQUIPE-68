package com.bitsystem.bitapp.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.nullable;
import static org.mockito.Mockito.when;

import com.bitsystem.bitapp.dto.SaudeDto;
import com.bitsystem.bitapp.integration.GeminiClient;
import com.bitsystem.bitapp.integration.N8NMentalHealthClient;
import com.bitsystem.bitapp.repository.HistoricoSaudeRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

/**
 * Testes da REGRA DE DERIVAÇÃO AO CVV — o comportamento mais sensível do produto.
 *
 * Garantias verificadas:
 *  - A derivação é GRADUADA e decidida SÓ pela nota semanal:
 *      nota 0-1 → REFORCADO, nota 2-3 → PREVENTIVO, nota 4-10 → nenhum (null).
 *  - O HUMOR nunca deriva (humor "sobrecarregado" + nota alta → sem derivação).
 *  - A IA / agente de acolhimento NUNCA altera derivarCvv (só fornece texto).
 *  - A Bean Validation rejeita notas fora de 0-10.
 *
 * O caminho de acolhimento é neutralizado (n8n indisponível, Gemini não
 * configurado, respostas curadas) para isolar a decisão determinística.
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class SaudeMentalServiceTest {

    @Mock private HistoricoSaudeRepository saudeRepository;
    @Mock private EmotionResponseProvider emotionResponseProvider;
    @Mock private FallbackStorage fallbackStorage;
    @Mock private GeminiClient geminiClient;
    @Mock private N8NMentalHealthClient mentalHealthClient;
    @Mock private ObjectMapper objectMapper;

    @InjectMocks private SaudeMentalService service;

    @BeforeEach
    void neutralizarAcolhimento() {
        // Força o fallback local e evita qualquer chamada de IA real.
        when(mentalHealthClient.process(any())).thenThrow(new RuntimeException("n8n off"));
        when(geminiClient.isConfigured()).thenReturn(false);
        when(emotionResponseProvider.resolve(anyString(), nullable(Integer.class), anyString()))
                .thenReturn(new SaudeDto.RawResponse("Estamos com você.", "Respire fundo."));
    }

    private SaudeDto.Response checkin(String humor, int nota) {
        return service.avaliarEstadoMental(
                new SaudeDto.Request(1L, humor, nota, "contexto qualquer", "pt"));
    }

    // Check-in diário (lote 4.1): sem nota semanal — usa Integer nulo, que o
    // helper acima (int primitivo) não consegue representar.
    private SaudeDto.Response checkinSemNota(String humor, String contexto) {
        return service.avaliarEstadoMental(
                new SaudeDto.Request(1L, humor, null, contexto, "pt"));
    }

    // ── REFORCADO: nota 0-1 ──────────────────────────────────────────────────
    @ParameterizedTest
    @ValueSource(ints = {0, 1})
    void nota0e1_derivaReforcado(int nota) {
        SaudeDto.Response r = checkin("feliz", nota);
        assertThat(r.derivarCvv()).isTrue();
        assertThat(r.nivelDerivacao()).isEqualTo("REFORCADO");
        assertThat(r.alerta()).isEqualTo("DERIVACAO_REFORCADA");
    }

    // ── PREVENTIVO: nota 2-3 ─────────────────────────────────────────────────
    @ParameterizedTest
    @ValueSource(ints = {2, 3})
    void nota2e3_derivaPreventivo(int nota) {
        SaudeDto.Response r = checkin("feliz", nota);
        assertThat(r.derivarCvv()).isTrue();
        assertThat(r.nivelDerivacao()).isEqualTo("PREVENTIVO");
        assertThat(r.alerta()).isEqualTo("DERIVACAO_PREVENTIVA");
    }

    // ── SEM DERIVAÇÃO: nota 4-10 ─────────────────────────────────────────────
    @ParameterizedTest
    @ValueSource(ints = {4, 7, 10})
    void nota4a10_naoDeriva(int nota) {
        SaudeDto.Response r = checkin("feliz", nota);
        assertThat(r.derivarCvv()).isFalse();
        assertThat(r.nivelDerivacao()).isNull();
        assertThat(r.alerta()).isEqualTo("ESTAVEL");
    }

    // ── O HUMOR NUNCA DERIVA ──────────────────────────────────────────────────
    @Test
    void humorSobrecarregadoComNotaAlta_naoDeriva() {
        SaudeDto.Response r = checkin("sobrecarregado", 8);
        assertThat(r.derivarCvv()).isFalse();
        assertThat(r.nivelDerivacao()).isNull();
    }

    // ── A IA / AGENTE NUNCA ALTERA A DERIVAÇÃO ───────────────────────────────
    @Test
    void textoDeAcolhimentoMencionandoCriseNaoAlteraDerivacao() {
        // Mesmo que o acolhimento fale em "crise" e "CVV 188", a nota 8 manda.
        when(emotionResponseProvider.resolve(anyString(), nullable(Integer.class), anyString()))
                .thenReturn(new SaudeDto.RawResponse(
                        "Você está em crise, procure o CVV 188 imediatamente!",
                        "Ligue 188 agora."));
        SaudeDto.Response r = checkin("triste", 8);
        assertThat(r.derivarCvv()).isFalse();
        assertThat(r.nivelDerivacao()).isNull();
    }

    // ── BEAN VALIDATION: rejeita notas fora de 0-10 ──────────────────────────
    @ParameterizedTest
    @ValueSource(ints = {-1, 11})
    void validacaoRejeitaNotaForaDoIntervalo(int notaInvalida) {
        Set<ConstraintViolation<SaudeDto.Request>> violacoes =
                validar(new SaudeDto.Request(1L, "feliz", notaInvalida, "ctx", "pt"));
        assertThat(violacoes).isNotEmpty();
    }

    @ParameterizedTest
    @ValueSource(ints = {0, 10})
    void validacaoAceitaNotasNosLimites(int notaValida) {
        Set<ConstraintViolation<SaudeDto.Request>> violacoes =
                validar(new SaudeDto.Request(1L, "feliz", notaValida, "ctx", "pt"));
        assertThat(violacoes).isEmpty();
    }

    // ── NOTA AUSENTE (lote 4.1): check-in diário sem nota semanal ────────────
    @Test
    void validacaoAceitaNotaAusente() {
        Set<ConstraintViolation<SaudeDto.Request>> violacoes =
                validar(new SaudeDto.Request(1L, "feliz", null, "ctx", "pt"));
        assertThat(violacoes).isEmpty();
    }

    @Test
    void checkinSemNotaNaoDeriva() {
        SaudeDto.Response r = checkinSemNota("feliz", "algum texto sobre o dia");
        assertThat(r.derivarCvv()).isFalse();
        assertThat(r.nivelDerivacao()).isNull();
        assertThat(r.alerta()).isEqualTo("ESTAVEL");
    }

    // ── LEITURA EMOCIONAL (lote 4.1): sempre preenchida, nunca deriva ────────
    @Test
    void leituraEmocionalSempreVemPreenchida() {
        SaudeDto.Response r = checkin("feliz", 8);
        assertThat(r.leituraEmocional()).isNotNull().isNotBlank();
    }

    @Test
    void leituraEmocionalNaoAlteraDerivacao() {
        when(emotionResponseProvider.resolve(anyString(), nullable(Integer.class), anyString()))
                .thenReturn(new SaudeDto.RawResponse("Mensagem A", "Ação A", "Leitura A"));
        SaudeDto.Response r1 = checkin("triste", 8);

        when(emotionResponseProvider.resolve(anyString(), nullable(Integer.class), anyString()))
                .thenReturn(new SaudeDto.RawResponse("Mensagem B bem diferente", "Ação B", "Leitura B, completamente distinta"));
        SaudeDto.Response r2 = checkin("triste", 8);

        assertThat(r1.nivelDerivacao()).isEqualTo(r2.nivelDerivacao());
        assertThat(r1.derivarCvv()).isEqualTo(r2.derivarCvv());
    }

    private Set<ConstraintViolation<SaudeDto.Request>> validar(SaudeDto.Request request) {
        try (ValidatorFactory factory = Validation.buildDefaultValidatorFactory()) {
            Validator validator = factory.getValidator();
            return validator.validate(request);
        }
    }
}