package com.bitsystem.bitapp.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.bitsystem.bitapp.domain.CareerAssessment;
import com.bitsystem.bitapp.dto.AssessmentDto;
import com.bitsystem.bitapp.integration.N8NAssessmentClient;
import com.bitsystem.bitapp.repository.CareerAssessmentRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

/**
 * Testes de AssessmentService — cobrem a persistência completa do assessment
 * (compatibilidade, nivel, pontosFortes, gaps, planoDesenvolvimento) tanto no
 * caminho feliz (n8n ok) quanto no caminho de fallback (n8n fora), replicando
 * o padrão try-banco/catch-memória de SaudeMentalService.avaliarEstadoMental.
 */
@ExtendWith(MockitoExtension.class)
class AssessmentServiceTest {

    @Mock private N8NAssessmentClient n8nClient;
    @Mock private CareerAssessmentRepository repository;
    @Mock private FallbackStorage fallbackStorage;

    private AssessmentService service;

    @BeforeEach
    void setUp() {
        // ObjectMapper real (não mockado) — os testes exercitam a serialização
        // JSON de verdade, não só o wiring.
        service = new AssessmentService(n8nClient, repository, fallbackStorage, new ObjectMapper());
    }

    private AssessmentDto.Request request() {
        return new AssessmentDto.Request(
            "João", 25, "Superior", "2 anos",
            List.of("Java", "Spring"), List.of("Comunicação"),
            List.of("Spring Boot"), "orientacao", "pt"
        );
    }

    private AssessmentDto.Response respostaCompleta() {
        return new AssessmentDto.Response(
            82, "Júnior Pleno",
            List.of("Conhecimento sólido em Java", "Boa comunicação"),
            List.of("Aprofundar em Cloud Native", "Testes automatizados"),
            List.of("Estudar Spring Cloud", "Praticar TDD")
        );
    }

    // ── DTO básico (testes originais, mantidos) ──────────────────────────────
    @Test
    void shouldValidateAssessmentDto() {
        var request = request();
        assertThat(request.nome()).isEqualTo("João");
        assertThat(request.idade()).isEqualTo(25);
    }

    @Test
    void shouldRejectEmptyNome() {
        var request = new AssessmentDto.Request(
            "", 25, "", "", List.of(), List.of(), List.of(), "", "pt"
        );
        assertThat(request.nome()).isEmpty();
    }

    // ── CAMINHO FELIZ (n8n ok): salva o assessment COMPLETO no banco ─────────
    @Test
    void processar_n8nOk_salvaAssessmentCompletoNoBanco() {
        when(n8nClient.process(any())).thenReturn(respostaCompleta());

        AssessmentDto.Response resposta = service.processar(request(), 7L);

        ArgumentCaptor<CareerAssessment> captor = ArgumentCaptor.forClass(CareerAssessment.class);
        verify(repository).save(captor.capture());
        CareerAssessment salvo = captor.getValue();

        assertThat(salvo.getUsuarioId()).isEqualTo(7L);
        assertThat(salvo.getCompatibilidade()).isEqualTo(82);
        assertThat(salvo.getNivel()).isEqualTo("Júnior Pleno");
        assertThat(salvo.getPontosFortes()).contains("Conhecimento sólido em Java", "Boa comunicação");
        assertThat(salvo.getGaps()).contains("Aprofundar em Cloud Native", "Testes automatizados");
        assertThat(salvo.getPlanoDesenvolvimento()).contains("Estudar Spring Cloud", "Praticar TDD");

        assertThat(resposta.compatibilidade()).isEqualTo(82);
        verify(fallbackStorage, never()).saveAssessment(any(), any());
    }

    // ── FALLBACK (n8n indisponível): também precisa salvar no banco ──────────
    @Test
    void processar_n8nIndisponivel_aindaAssimSalvaNoBanco() {
        when(n8nClient.process(any())).thenThrow(new RuntimeException("n8n off"));

        AssessmentDto.Response resposta = service.processar(request(), 9L);

        ArgumentCaptor<CareerAssessment> captor = ArgumentCaptor.forClass(CareerAssessment.class);
        verify(repository).save(captor.capture());
        CareerAssessment salvo = captor.getValue();

        assertThat(salvo.getUsuarioId()).isEqualTo(9L);
        assertThat(salvo.getCompatibilidade()).isEqualTo(resposta.compatibilidade());
        assertThat(salvo.getNivel()).isEqualTo(resposta.nivel());
        assertThat(salvo.getPontosFortes()).isNotBlank();
        verify(fallbackStorage, never()).saveAssessment(any(), any());
    }

    // ── FALLBACK DUPLO (n8n fora E banco fora): só aí usa o FallbackStorage ──
    @Test
    void processar_n8nEBancoIndisponiveis_usaFallbackStorage() {
        when(n8nClient.process(any())).thenThrow(new RuntimeException("n8n off"));
        doThrow(new RuntimeException("db off")).when(repository).save(any());

        AssessmentDto.Response resposta = service.processar(request(), 3L);

        verify(fallbackStorage).saveAssessment(eq(3L), eq(resposta));
    }

    // ── CAMINHO FELIZ, MAS BANCO FORA: cai para o FallbackStorage ────────────
    @Test
    void processar_n8nOkMasBancoIndisponivel_usaFallbackStorage() {
        AssessmentDto.Response respostaN8n = respostaCompleta();
        when(n8nClient.process(any())).thenReturn(respostaN8n);
        doThrow(new RuntimeException("db off")).when(repository).save(any());

        service.processar(request(), 5L);

        verify(fallbackStorage).saveAssessment(5L, respostaN8n);
    }

    // ── HISTÓRICO: banco disponível, campos deserializados corretamente ──────
    @Test
    void buscarHistorico_bancoDisponivel_deserializaCamposCorretamente() {
        CareerAssessment entidade = CareerAssessment.builder()
                .usuarioId(1L)
                .compatibilidade(75)
                .nivel("Júnior Trainee")
                .pontosFortes("[\"Ponto forte A\"]")
                .gaps("[\"Gap A\",\"Gap B\"]")
                .planoDesenvolvimento("[\"Ação 1\"]")
                .build();
        when(repository.findByUsuarioIdOrderByCreatedAtDesc(1L)).thenReturn(List.of(entidade));

        List<AssessmentDto.HistoricoResponse> historico = service.buscarHistorico(1L);

        assertThat(historico).hasSize(1);
        AssessmentDto.HistoricoResponse item = historico.get(0);
        assertThat(item.compatibilidade()).isEqualTo(75);
        assertThat(item.nivel()).isEqualTo("Júnior Trainee");
        assertThat(item.pontosFortes()).containsExactly("Ponto forte A");
        assertThat(item.gaps()).containsExactly("Gap A", "Gap B");
        assertThat(item.planoDesenvolvimento()).containsExactly("Ação 1");
    }

    // ── HISTÓRICO: banco indisponível, cai para o FallbackStorage ────────────
    @Test
    void buscarHistorico_bancoIndisponivel_usaFallbackStorage() {
        when(repository.findByUsuarioIdOrderByCreatedAtDesc(2L)).thenThrow(new RuntimeException("db off"));
        FallbackStorage.AssessmentRecord registro = new FallbackStorage.AssessmentRecord(
                1L, 2L, 60, "Júnior Trainee",
                List.of("Forte X"), List.of("Gap X"), List.of("Plano X"),
                LocalDateTime.now()
        );
        when(fallbackStorage.findAssessmentsByUserId(2L)).thenReturn(List.of(registro));

        List<AssessmentDto.HistoricoResponse> historico = service.buscarHistorico(2L);

        assertThat(historico).hasSize(1);
        assertThat(historico.get(0).compatibilidade()).isEqualTo(60);
        assertThat(historico.get(0).pontosFortes()).containsExactly("Forte X");
    }

    // ── HISTÓRICO: campos nulos (linha antiga, ex. pré-migração em produção) ─
    @Test
    void buscarHistorico_camposNulos_retornaListasVaziasSemExcecao() {
        CareerAssessment entidadeAntiga = CareerAssessment.builder()
                .usuarioId(4L)
                .compatibilidade(50)
                .nivel("Júnior Trainee")
                .pontosFortes(null)
                .gaps(null)
                .planoDesenvolvimento(null)
                .build();
        when(repository.findByUsuarioIdOrderByCreatedAtDesc(4L)).thenReturn(List.of(entidadeAntiga));

        List<AssessmentDto.HistoricoResponse> historico = service.buscarHistorico(4L);

        assertThat(historico).hasSize(1);
        AssessmentDto.HistoricoResponse item = historico.get(0);
        assertThat(item.pontosFortes()).isEmpty();
        assertThat(item.gaps()).isEmpty();
        assertThat(item.planoDesenvolvimento()).isEmpty();
    }

    // ── HISTÓRICO: sem registros, retorna lista vazia (estado vazio) ─────────
    @Test
    void buscarHistorico_semRegistros_retornaListaVazia() {
        when(repository.findByUsuarioIdOrderByCreatedAtDesc(99L)).thenReturn(List.of());

        List<AssessmentDto.HistoricoResponse> historico = service.buscarHistorico(99L);

        assertThat(historico).isEmpty();
    }
}
