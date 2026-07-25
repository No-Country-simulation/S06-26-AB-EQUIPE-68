package com.bitsystem.bitapp.service;

import com.bitsystem.bitapp.domain.Mentor;
import com.bitsystem.bitapp.domain.SolicitacaoMentoria;
import com.bitsystem.bitapp.dto.MentorDto;
import com.bitsystem.bitapp.dto.SolicitacaoMentoriaDto;
import com.bitsystem.bitapp.exception.BusinessException;
import com.bitsystem.bitapp.repository.MentorRepository;
import com.bitsystem.bitapp.repository.SolicitacaoMentoriaRepository;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class MentoriaService {

    private static final Logger log = LoggerFactory.getLogger(MentoriaService.class);

    private static final List<String> ORDEM_STATUS = List.of(
        SolicitacaoMentoria.AGUARDANDO_CONFIRMACAO,
        SolicitacaoMentoria.CONFIRMADA,
        SolicitacaoMentoria.CONCLUIDA
    );

    private final MentorRepository mentorRepository;
    private final SolicitacaoMentoriaRepository repository;
    private final FallbackStorage fallbackStorage;

    public MentoriaService(MentorRepository mentorRepository, SolicitacaoMentoriaRepository repository,
            FallbackStorage fallbackStorage) {
        this.mentorRepository = mentorRepository;
        this.repository = repository;
        this.fallbackStorage = fallbackStorage;
    }

    public List<MentorDto> listarMentores() {
        return mentorRepository.findByAtivoTrue().stream().map(MentorDto::from).toList();
    }

    /**
     * Cria a solicitação (mesmo padrão try-banco/catch-memória de
     * AssessmentService.salvarAssessment): tenta o banco primeiro, só cai
     * para o FallbackStorage se o banco estiver indisponível.
     */
    public SolicitacaoMentoriaDto.Response solicitar(SolicitacaoMentoriaDto.Request request, Long usuarioId) {
        Mentor mentor = mentorRepository.findById(request.mentorId())
                .orElseThrow(() -> new BusinessException("MENTOR_NAO_ENCONTRADO",
                        "Mentor não encontrado: " + request.mentorId()));

        try {
            SolicitacaoMentoria solicitacao = SolicitacaoMentoria.builder()
                    .usuarioId(usuarioId)
                    .mentorId(mentor.getId())
                    .areaSolicitada(request.areaSolicitada())
                    .mensagem(request.mensagem())
                    .build();
            solicitacao = repository.save(solicitacao);
            log.info("[MentoriaService] Solicitação salva no banco: usuarioId={}, mentorId={}", usuarioId, mentor.getId());
            return toResponse(solicitacao, mentor.getNome());
        } catch (Exception dbEx) {
            log.warn("[MentoriaService] Banco indisponível, salvando solicitação em memória: {}", dbEx.getMessage());
            FallbackStorage.MentoriaRecord record = fallbackStorage.saveMentoria(
                    usuarioId, mentor.getId(), request.areaSolicitada(), request.mensagem());
            return new SolicitacaoMentoriaDto.Response(
                    record.id(), record.usuarioId(), record.mentorId(), mentor.getNome(),
                    record.areaSolicitada(), record.mensagem(), record.status(), record.createdAt());
        }
    }

    public List<SolicitacaoMentoriaDto.HistoricoResponse> buscarHistorico(Long usuarioId) {
        List<SolicitacaoMentoriaDto.HistoricoResponse> resultado = new ArrayList<>();

        try {
            List<SolicitacaoMentoria> registros = repository.findByUsuarioIdOrderByCreatedAtDesc(usuarioId);
            for (SolicitacaoMentoria s : registros) {
                resultado.add(new SolicitacaoMentoriaDto.HistoricoResponse(
                        s.getId(), s.getMentorId(), mentorNome(s.getMentorId()),
                        s.getAreaSolicitada(), s.getMensagem(), s.getStatus(), s.getCreatedAt()));
            }
        } catch (Exception ex) {
            log.warn("[MentoriaService] DB indisponivel, buscando historico em memoria: {}", ex.getMessage());
            List<FallbackStorage.MentoriaRecord> fallbackRecords = fallbackStorage.findMentoriasByUserId(usuarioId);
            for (FallbackStorage.MentoriaRecord r : fallbackRecords) {
                resultado.add(new SolicitacaoMentoriaDto.HistoricoResponse(
                        r.id(), r.mentorId(), mentorNome(r.mentorId()),
                        r.areaSolicitada(), r.mensagem(), r.status(), r.createdAt()));
            }
        }

        return resultado;
    }

    /**
     * Avança o status em uma casa (AGUARDANDO_CONFIRMACAO -> CONFIRMADA ->
     * CONCLUIDA), sem pular nem voltar. Status desconhecido ou transição fora
     * de ordem viram BusinessException -> 422 (padrão do sistema, não 400).
     */
    public SolicitacaoMentoriaDto.Response atualizarStatus(Long id, String novoStatus) {
        try {
            Optional<SolicitacaoMentoria> encontrada = repository.findById(id);
            if (encontrada.isPresent()) {
                SolicitacaoMentoria solicitacao = encontrada.get();
                validarTransicao(solicitacao.getStatus(), novoStatus);
                solicitacao.setStatus(novoStatus);
                solicitacao = repository.save(solicitacao);
                return toResponse(solicitacao, mentorNome(solicitacao.getMentorId()));
            }
        } catch (BusinessException be) {
            throw be;
        } catch (Exception dbEx) {
            log.warn("[MentoriaService] Banco indisponível ao atualizar status, tentando memória: {}", dbEx.getMessage());
        }

        FallbackStorage.MentoriaRecord existente = fallbackStorage.findMentoriaById(id)
                .orElseThrow(() -> new BusinessException("SOLICITACAO_NAO_ENCONTRADA",
                        "Solicitação de mentoria não encontrada: " + id));
        validarTransicao(existente.status(), novoStatus);
        FallbackStorage.MentoriaRecord atualizado = fallbackStorage.updateMentoriaStatus(id, novoStatus);
        return new SolicitacaoMentoriaDto.Response(
                atualizado.id(), atualizado.usuarioId(), atualizado.mentorId(), mentorNome(atualizado.mentorId()),
                atualizado.areaSolicitada(), atualizado.mensagem(), atualizado.status(), atualizado.createdAt());
    }

    private void validarTransicao(String atual, String novo) {
        int atualIdx = ORDEM_STATUS.indexOf(atual);
        int novoIdx = ORDEM_STATUS.indexOf(novo);
        if (novoIdx < 0) {
            throw new BusinessException("STATUS_INVALIDO", "Status inválido: " + novo);
        }
        if (novoIdx != atualIdx + 1) {
            throw new BusinessException("TRANSICAO_INVALIDA",
                    "Transição de status inválida: " + atual + " -> " + novo);
        }
    }

    private String mentorNome(Long mentorId) {
        try {
            return mentorRepository.findById(mentorId).map(Mentor::getNome).orElse("Mentor #" + mentorId);
        } catch (Exception ex) {
            return "Mentor #" + mentorId;
        }
    }

    private SolicitacaoMentoriaDto.Response toResponse(SolicitacaoMentoria s, String mentorNome) {
        return new SolicitacaoMentoriaDto.Response(
                s.getId(), s.getUsuarioId(), s.getMentorId(), mentorNome,
                s.getAreaSolicitada(), s.getMensagem(), s.getStatus(), s.getCreatedAt());
    }
}
