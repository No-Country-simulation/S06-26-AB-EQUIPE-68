package com.bitsystem.bitapp.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.nullable;
import static org.mockito.Mockito.when;

import com.bitsystem.bitapp.dto.SaudeDto;
import com.bitsystem.bitapp.integration.GeminiClient;
import com.bitsystem.bitapp.integration.N8NMentalHealthClient;
import com.bitsystem.bitapp.model.HistoricoSaude;
import com.bitsystem.bitapp.repository.HistoricoSaudeRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
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
 * Testes da REGRA DE DERIVAÇÃO AO CVV (gatilho imediato) e da TENDÊNCIA
 * SEMANAL (CVV v2) — os comportamentos mais sensíveis do produto.
 *
 * Garantias verificadas:
 *  - Gatilho imediato decidido SÓ pela nota do check-in (escala 9/7/5/3/1):
 *      nota 1 → REFORCADO, nota 3 → PREVENTIVO, nota 5/7/9/ausente → nenhum.
 *  - Check-in só-texto (nota null) nunca deriva e nunca entra na agregação.
 *  - A IA / agente de acolhimento NUNCA altera derivarCvv (só fornece texto).
 *  - A Bean Validation rejeita notas fora da escala válida.
 *  - Tendência semanal: 3+ dos até 5 dias-com-registro mais recentes, dentro
 *    de uma janela de 7 dias corridos, com nota-do-dia <=3.
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
        when(emotionResponseProvider.resolve(nullable(Integer.class), anyString()))
                .thenReturn(new SaudeDto.RawResponse("Estamos com você.", "Respire fundo."));
        // Histórico vazio por padrão -> tendenciaSemana=false, salvo quando um
        // teste específico configura um histórico para testar a agregação.
        when(saudeRepository.findByUserIdOrderByCreatedAtDesc(any())).thenReturn(List.of());
    }

    private SaudeDto.Response checkin(Integer nota, String contexto) {
        return service.avaliarEstadoMental(new SaudeDto.Request(1L, nota, contexto, "pt"));
    }

    // ── GATILHO IMEDIATO: cada nota da escala → efeito correto ───────────────
    @Test
    void nota1_derivaReforcado() {
        SaudeDto.Response r = checkin(1, "contexto qualquer");
        assertThat(r.derivarCvv()).isTrue();
        assertThat(r.nivelDerivacao()).isEqualTo("REFORCADO");
        assertThat(r.alerta()).isEqualTo("DERIVACAO_REFORCADA");
    }

    @Test
    void nota3_derivaPreventivo() {
        SaudeDto.Response r = checkin(3, "contexto qualquer");
        assertThat(r.derivarCvv()).isTrue();
        assertThat(r.nivelDerivacao()).isEqualTo("PREVENTIVO");
        assertThat(r.alerta()).isEqualTo("DERIVACAO_PREVENTIVA");
    }

    @ParameterizedTest
    @ValueSource(ints = {5, 7, 9})
    void nota5e7e9_naoDeriva(int nota) {
        SaudeDto.Response r = checkin(nota, "contexto qualquer");
        assertThat(r.derivarCvv()).isFalse();
        assertThat(r.nivelDerivacao()).isNull();
        assertThat(r.alerta()).isEqualTo("ESTAVEL");
    }

    @Test
    void nota1_derivaReforcado_mesmoSemHistorico() {
        // Histórico vazio (default do @BeforeEach) — gatilho imediato não depende de histórico.
        SaudeDto.Response r = checkin(1, null);
        assertThat(r.derivarCvv()).isTrue();
        assertThat(r.nivelDerivacao()).isEqualTo("REFORCADO");
    }

    // ── CHECK-IN SÓ-TEXTO: nota null → nenhum gatilho, nenhum ponto de agregação ──
    @Test
    void checkinSoTexto_notaNull_naoDerivaENaoContaNaAgregacao() {
        SaudeDto.Response r = checkin(null, "só um desabafo, sem emoji");
        assertThat(r.derivarCvv()).isFalse();
        assertThat(r.nivelDerivacao()).isNull();
        assertThat(r.alerta()).isEqualTo("ESTAVEL");
        assertThat(r.tendenciaSemana()).isFalse();
    }

    // ── A IA / AGENTE NUNCA ALTERA A DERIVAÇÃO (invariante) ──────────────────
    @Test
    void textoDeAcolhimentoExtremoComNotaAlta_naoAlteraDerivacao() {
        // Texto grave simulando conteúdo de crise, mas nota=9 (Muito feliz) —
        // só a nota decide; a IA/agente nunca influenciam a derivação.
        when(emotionResponseProvider.resolve(nullable(Integer.class), anyString()))
                .thenReturn(new SaudeDto.RawResponse(
                        "Você está em crise, procure o CVV 188 imediatamente!",
                        "Ligue 188 agora."));
        SaudeDto.Response r = checkin(9, "não aguento mais viver, quero desistir de tudo");
        assertThat(r.derivarCvv()).isFalse();
        assertThat(r.nivelDerivacao()).isNull();
        assertThat(r.tendenciaSemana()).isFalse();
    }

    // ── BEAN VALIDATION: rejeita notas fora da escala válida (só 1/3/5/7/9) ──
    @ParameterizedTest
    @ValueSource(ints = {0, 2, 4, 6, 8, 10, -1})
    void validacaoRejeitaNotaForaDaEscala(int notaInvalida) {
        Set<ConstraintViolation<SaudeDto.Request>> violacoes =
                validar(new SaudeDto.Request(1L, notaInvalida, "ctx", "pt"));
        assertThat(violacoes).isNotEmpty();
    }

    @ParameterizedTest
    @ValueSource(ints = {1, 3, 5, 7, 9})
    void validacaoAceitaNotasDaEscala(int notaValida) {
        Set<ConstraintViolation<SaudeDto.Request>> violacoes =
                validar(new SaudeDto.Request(1L, notaValida, "ctx", "pt"));
        assertThat(violacoes).isEmpty();
    }

    @Test
    void validacaoAceitaNotaAusente() {
        Set<ConstraintViolation<SaudeDto.Request>> violacoes =
                validar(new SaudeDto.Request(1L, null, "ctx", "pt"));
        assertThat(violacoes).isEmpty();
    }

    // ── LEITURA EMOCIONAL: sempre preenchida, nunca deriva ───────────────────
    @Test
    void leituraEmocionalSempreVemPreenchida() {
        SaudeDto.Response r = checkin(9, "dia produtivo");
        assertThat(r.leituraEmocional()).isNotNull().isNotBlank();
    }

    @Test
    void leituraEmocionalNaoAlteraDerivacao() {
        when(emotionResponseProvider.resolve(nullable(Integer.class), anyString()))
                .thenReturn(new SaudeDto.RawResponse("Mensagem A", "Ação A", "Leitura A"));
        SaudeDto.Response r1 = checkin(9, "texto");

        when(emotionResponseProvider.resolve(nullable(Integer.class), anyString()))
                .thenReturn(new SaudeDto.RawResponse("Mensagem B bem diferente", "Ação B", "Leitura B, completamente distinta"));
        SaudeDto.Response r2 = checkin(9, "texto");

        assertThat(r1.nivelDerivacao()).isEqualTo(r2.nivelDerivacao());
        assertThat(r1.derivarCvv()).isEqualTo(r2.derivarCvv());
    }

    // ── TENDÊNCIA: wiring de ponta a ponta via avaliarEstadoMental ───────────
    @Test
    void avaliarEstadoMental_calculaTendenciaAPartirDoHistoricoPersistido() {
        LocalDateTime agora = LocalDateTime.now();
        List<HistoricoSaude> historicoRuim = List.of(
                historico(3, agora.minusDays(1)),
                historico(1, agora.minusDays(2)),
                historico(3, agora.minusDays(3))
        );
        when(saudeRepository.findByUserIdOrderByCreatedAtDesc(1L)).thenReturn(historicoRuim);

        SaudeDto.Response r = checkin(9, "hoje foi um dia bom"); // nota 9 hoje: sem gatilho imediato

        assertThat(r.nivelDerivacao()).isNull();
        assertThat(r.tendenciaSemana()).isTrue();
    }

    private HistoricoSaude historico(Integer nota, LocalDateTime createdAt) {
        return HistoricoSaude.builder().userId(1L).nota(nota).createdAt(createdAt).build();
    }

    private Set<ConstraintViolation<SaudeDto.Request>> validar(SaudeDto.Request request) {
        try (ValidatorFactory factory = Validation.buildDefaultValidatorFactory()) {
            Validator validator = factory.getValidator();
            return validator.validate(request);
        }
    }

    // ════════════════════════════════════════════════════════════════════════
    //  TENDÊNCIA SEMANAL — testes diretos da função pura de agregação
    // ════════════════════════════════════════════════════════════════════════

    private static final LocalDate HOJE = LocalDate.of(2026, 7, 5);

    @Test
    void tendencia_3diasRuinsEm5ComRegistro_dispara() {
        List<SaudeDto.HistoricoResponse> historico = List.of(
                registro(HOJE, 3),
                registro(HOJE.minusDays(1), 9),
                registro(HOJE.minusDays(2), 1),
                registro(HOJE.minusDays(3), 7),
                registro(HOJE.minusDays(4), 3)
        );
        assertThat(SaudeMentalService.calcularTendenciaSemana(historico, HOJE)).isTrue();
    }

    @Test
    void tendencia_2diasRuins_naoDispara() {
        List<SaudeDto.HistoricoResponse> historico = List.of(
                registro(HOJE, 3),
                registro(HOJE.minusDays(1), 9),
                registro(HOJE.minusDays(2), 1),
                registro(HOJE.minusDays(3), 7),
                registro(HOJE.minusDays(4), 9)
        );
        assertThat(SaudeMentalService.calcularTendenciaSemana(historico, HOJE)).isFalse();
    }

    @Test
    void tendencia_diasRuinsForaDaJanelaDe7Corridos_naoContam() {
        List<SaudeDto.HistoricoResponse> historico = List.of(
                registro(HOJE, 3),
                registro(HOJE.minusDays(1), 3),
                registro(HOJE.minusDays(8), 1),
                registro(HOJE.minusDays(9), 1),
                registro(HOJE.minusDays(10), 1)
        );
        // Só 2 dias-com-registro dentro da janela de 7 corridos -> <3, não dispara
        // (se os 3 dias fora da janela contassem incorretamente, dispararia).
        assertThat(SaudeMentalService.calcularTendenciaSemana(historico, HOJE)).isFalse();
    }

    @Test
    void tendencia_menosDe3DiasDeHistorico_nuncaAvaliada() {
        List<SaudeDto.HistoricoResponse> historico = List.of(
                registro(HOJE, 1),
                registro(HOJE.minusDays(1), 1)
        );
        assertThat(SaudeMentalService.calcularTendenciaSemana(historico, HOJE)).isFalse();
    }

    @Test
    void tendencia_multiplosCheckinsNoMesmoDia_agregacaoPegaOPior() {
        List<SaudeDto.HistoricoResponse> historico = List.of(
                registro(HOJE, 9),
                registro(HOJE, 1), // pior do dia = 1
                registro(HOJE.minusDays(1), 3),
                registro(HOJE.minusDays(2), 3),
                registro(HOJE.minusDays(3), 9)
        );
        // Dias-com-registro: hoje(1), d-1(3), d-2(3), d-3(9) -> 3 de 4 <=3 -> dispara
        assertThat(SaudeMentalService.calcularTendenciaSemana(historico, HOJE)).isTrue();
    }

    @Test
    void tendencia_gapDeDiasSemRegistro_naoImpedeContagem() {
        List<SaudeDto.HistoricoResponse> historico = List.of(
                registro(HOJE.minusDays(6), 3),
                registro(HOJE.minusDays(5), 1),
                registro(HOJE.minusDays(4), 3)
                // HOJE-3 .. HOJE: sem nenhum check-in (gap) — não deve impedir a contagem.
        );
        // Dias-com-registro (o que importa) != dias corridos: 3 dias-com-registro,
        // todos dentro da janela de 7 corridos e todos <=3 -> dispara, mesmo com
        // lacuna de 4 dias sem check-in entre eles e a avaliação de hoje.
        assertThat(SaudeMentalService.calcularTendenciaSemana(historico, HOJE)).isTrue();
    }

    @Test
    void tendencia_fronteiraDeDia_23h59e00h01_saoDiasDistintos() {
        List<SaudeDto.HistoricoResponse> historico = List.of(
                registro(HOJE.minusDays(3), 3),
                new SaudeDto.HistoricoResponse(2L, 3, "ctx", false, HOJE.minusDays(2).atTime(23, 59)),
                new SaudeDto.HistoricoResponse(3L, 1, "ctx", false, HOJE.minusDays(1).atTime(0, 1))
        );
        // Os dois últimos check-ins são separados por só 2 minutos, mas cruzam a
        // meia-noite -> agregação é por DATA (LocalDate), não por janela de 24h,
        // então viram 2 dias distintos. Com os 3 registros como 3 dias-com-registro
        // distintos, todos <=3, a tendência deve disparar. Se a agregação
        // (incorretamente) tratasse os dois check-ins como o mesmo dia, restariam
        // só 2 dias-com-registro (<3) e o teste falharia.
        assertThat(SaudeMentalService.calcularTendenciaSemana(historico, HOJE)).isTrue();
    }

    @Test
    void tendencia_checkinSoTexto_naoContaNaAgregacao() {
        List<SaudeDto.HistoricoResponse> historico = List.of(
                registro(HOJE, null),          // só texto: ignorado, não vira "dia com registro"
                registro(HOJE.minusDays(1), 1),
                registro(HOJE.minusDays(2), 3),
                registro(HOJE.minusDays(3), 3)
        );
        // 3 dias-com-registro numérico (d-1,d-2,d-3), todos <=3 -> dispara
        assertThat(SaudeMentalService.calcularTendenciaSemana(historico, HOJE)).isTrue();
    }

    private static SaudeDto.HistoricoResponse registro(LocalDate dia, Integer nota) {
        return new SaudeDto.HistoricoResponse(1L, nota, "ctx", false, dia.atTime(10, 0));
    }
}
