package com.bitsystem.bitapp.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.bitsystem.bitapp.domain.Mentor;
import com.bitsystem.bitapp.domain.SolicitacaoMentoria;
import com.bitsystem.bitapp.dto.MentorDto;
import com.bitsystem.bitapp.dto.SolicitacaoMentoriaDto;
import com.bitsystem.bitapp.exception.BusinessException;
import com.bitsystem.bitapp.repository.MentorRepository;
import com.bitsystem.bitapp.repository.SolicitacaoMentoriaRepository;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

/**
 * Testes de MentoriaService — cobrem o padrão try-banco/catch-memória
 * (igual AssessmentService) tanto na criação quanto no histórico, e a
 * transição sequencial única de status (AGUARDANDO_CONFIRMACAO ->
 * CONFIRMADA -> CONCLUIDA, sem pular, sem voltar).
 */
@ExtendWith(MockitoExtension.class)
class MentoriaServiceTest {

    @Mock private MentorRepository mentorRepository;
    @Mock private SolicitacaoMentoriaRepository repository;
    @Mock private FallbackStorage fallbackStorage;

    private MentoriaService service;

    @BeforeEach
    void setUp() {
        service = new MentoriaService(mentorRepository, repository, fallbackStorage);
    }

    private Mentor mentor(Long id) {
        Mentor m = new Mentor("André Teixeira", "Backend", "Java,DevOps,IA", "https://meet.google.com/kin-zmkx-rhs");
        m.setId(id);
        return m;
    }

    private SolicitacaoMentoriaDto.Request request(Long mentorId) {
        return new SolicitacaoMentoriaDto.Request(mentorId, "Java", "Quero orientação de carreira");
    }

    // ── listarMentores ────────────────────────────────────────────────────
    @Test
    void listarMentores_separaAreasAtendidasEmLista() {
        when(mentorRepository.findByAtivoTrue()).thenReturn(List.of(mentor(1L)));

        List<MentorDto> mentores = service.listarMentores();

        assertThat(mentores).hasSize(1);
        assertThat(mentores.get(0).areasAtendidas()).containsExactly("Java", "DevOps", "IA");
    }

    // ── solicitar ─────────────────────────────────────────────────────────
    @Test
    void solicitar_mentorNaoEncontrado_lancaBusinessException() {
        when(mentorRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.solicitar(request(99L), 1L))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("99");
    }

    @Test
    void solicitar_bancoOk_salvaComStatusAguardandoConfirmacao() {
        when(mentorRepository.findById(1L)).thenReturn(Optional.of(mentor(1L)));
        when(repository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        service.solicitar(request(1L), 7L);

        ArgumentCaptor<SolicitacaoMentoria> captor = ArgumentCaptor.forClass(SolicitacaoMentoria.class);
        verify(repository).save(captor.capture());
        SolicitacaoMentoria salva = captor.getValue();

        assertThat(salva.getUsuarioId()).isEqualTo(7L);
        assertThat(salva.getMentorId()).isEqualTo(1L);
        assertThat(salva.getAreaSolicitada()).isEqualTo("Java");
        assertThat(salva.getStatus()).isEqualTo(SolicitacaoMentoria.AGUARDANDO_CONFIRMACAO);
        verify(fallbackStorage, never()).saveMentoria(any(), any(), any(), any());
    }

    @Test
    void solicitar_bancoIndisponivel_usaFallbackStorage() {
        when(mentorRepository.findById(1L)).thenReturn(Optional.of(mentor(1L)));
        doThrow(new RuntimeException("db off")).when(repository).save(any());
        when(fallbackStorage.saveMentoria(7L, 1L, "Java", "Quero orientação de carreira"))
                .thenReturn(new FallbackStorage.MentoriaRecord(
                        1L, 7L, 1L, "Java", "Quero orientação de carreira",
                        SolicitacaoMentoria.AGUARDANDO_CONFIRMACAO, LocalDateTime.now()));

        SolicitacaoMentoriaDto.Response resposta = service.solicitar(request(1L), 7L);

        assertThat(resposta.status()).isEqualTo(SolicitacaoMentoria.AGUARDANDO_CONFIRMACAO);
        verify(fallbackStorage).saveMentoria(7L, 1L, "Java", "Quero orientação de carreira");
    }

    // ── buscarHistorico ───────────────────────────────────────────────────
    @Test
    void buscarHistorico_bancoDisponivel_retornaOrdenadoPeloRepository() {
        SolicitacaoMentoria s1 = SolicitacaoMentoria.builder()
                .usuarioId(1L).mentorId(1L).areaSolicitada("Java").status(SolicitacaoMentoria.CONFIRMADA).build();
        SolicitacaoMentoria s2 = SolicitacaoMentoria.builder()
                .usuarioId(1L).mentorId(1L).areaSolicitada("IA").status(SolicitacaoMentoria.AGUARDANDO_CONFIRMACAO).build();
        when(repository.findByUsuarioIdOrderByCreatedAtDesc(1L)).thenReturn(List.of(s1, s2));
        when(mentorRepository.findById(1L)).thenReturn(Optional.of(mentor(1L)));

        List<SolicitacaoMentoriaDto.HistoricoResponse> historico = service.buscarHistorico(1L);

        assertThat(historico).hasSize(2);
        assertThat(historico.get(0).areaSolicitada()).isEqualTo("Java");
        assertThat(historico.get(1).areaSolicitada()).isEqualTo("IA");
        assertThat(historico.get(0).mentorNome()).isEqualTo("André Teixeira");
    }

    @Test
    void buscarHistorico_bancoIndisponivel_usaFallbackStorage() {
        when(repository.findByUsuarioIdOrderByCreatedAtDesc(2L)).thenThrow(new RuntimeException("db off"));
        FallbackStorage.MentoriaRecord registro = new FallbackStorage.MentoriaRecord(
                1L, 2L, 1L, "Java", "msg", SolicitacaoMentoria.AGUARDANDO_CONFIRMACAO, LocalDateTime.now());
        when(fallbackStorage.findMentoriasByUserId(2L)).thenReturn(List.of(registro));
        when(mentorRepository.findById(1L)).thenReturn(Optional.of(mentor(1L)));

        List<SolicitacaoMentoriaDto.HistoricoResponse> historico = service.buscarHistorico(2L);

        assertThat(historico).hasSize(1);
        assertThat(historico.get(0).areaSolicitada()).isEqualTo("Java");
    }

    // ── atualizarStatus ───────────────────────────────────────────────────
    @Test
    void atualizarStatus_transicaoValida_avancaParaConfirmada() {
        SolicitacaoMentoria existente = SolicitacaoMentoria.builder()
                .usuarioId(1L).mentorId(1L).areaSolicitada("Java")
                .status(SolicitacaoMentoria.AGUARDANDO_CONFIRMACAO).build();
        existente.setId(10L);
        when(repository.findById(10L)).thenReturn(Optional.of(existente));
        when(repository.save(any())).thenAnswer(inv -> inv.getArgument(0));
        when(mentorRepository.findById(1L)).thenReturn(Optional.of(mentor(1L)));

        SolicitacaoMentoriaDto.Response resposta = service.atualizarStatus(10L, SolicitacaoMentoria.CONFIRMADA);

        assertThat(resposta.status()).isEqualTo(SolicitacaoMentoria.CONFIRMADA);
    }

    @Test
    void atualizarStatus_pulandoEtapa_lancaBusinessException() {
        SolicitacaoMentoria existente = SolicitacaoMentoria.builder()
                .usuarioId(1L).mentorId(1L).areaSolicitada("Java")
                .status(SolicitacaoMentoria.AGUARDANDO_CONFIRMACAO).build();
        existente.setId(11L);
        when(repository.findById(11L)).thenReturn(Optional.of(existente));

        assertThatThrownBy(() -> service.atualizarStatus(11L, SolicitacaoMentoria.CONCLUIDA))
                .isInstanceOf(BusinessException.class);
        verify(repository, never()).save(any());
    }

    @Test
    void atualizarStatus_statusDesconhecido_lancaBusinessException() {
        SolicitacaoMentoria existente = SolicitacaoMentoria.builder()
                .usuarioId(1L).mentorId(1L).areaSolicitada("Java")
                .status(SolicitacaoMentoria.AGUARDANDO_CONFIRMACAO).build();
        existente.setId(12L);
        when(repository.findById(12L)).thenReturn(Optional.of(existente));

        assertThatThrownBy(() -> service.atualizarStatus(12L, "STATUS_QUE_NAO_EXISTE"))
                .isInstanceOf(BusinessException.class);
    }

    @Test
    void atualizarStatus_solicitacaoNaoEncontradaNemNoBancoNemNaMemoria_lancaBusinessException() {
        when(repository.findById(999L)).thenReturn(Optional.empty());
        when(fallbackStorage.findMentoriaById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.atualizarStatus(999L, SolicitacaoMentoria.CONFIRMADA))
                .isInstanceOf(BusinessException.class);
    }
}
