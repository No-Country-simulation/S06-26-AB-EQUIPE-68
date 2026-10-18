package com.bitsystem.bitapp.service;

import com.bitsystem.bitapp.repository.CandidaturaVagaRepository;
import com.bitsystem.bitapp.repository.CareerAssessmentRepository;
import com.bitsystem.bitapp.repository.HistoricoSaudeRepository;
import com.bitsystem.bitapp.repository.InscricaoCursoRepository;
import com.bitsystem.bitapp.repository.SolicitacaoMentoriaRepository;
import com.bitsystem.bitapp.repository.UserSessionRepository;
import java.util.Collection;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Apaga tudo o que pertence a um conjunto de usuários (menos a própria conta):
 * check-ins, avaliações, solicitações de mentoria, inscrições em cursos,
 * candidaturas e sessões. Usado pelo reset das contas de demonstração e pela
 * retenção de visitantes. Junta-se à transação de quem chama, para que o reset
 * não fique pela metade.
 *
 * Nenhuma dessas tabelas tem chave estrangeira para users (guardam só o id),
 * então a ordem das exclusões não importa.
 */
@Service
public class DadosDoUsuarioService {

    private final HistoricoSaudeRepository saudeRepository;
    private final CareerAssessmentRepository assessmentRepository;
    private final SolicitacaoMentoriaRepository mentoriaRepository;
    private final InscricaoCursoRepository inscricaoRepository;
    private final CandidaturaVagaRepository candidaturaRepository;
    private final UserSessionRepository sessionRepository;

    public DadosDoUsuarioService(
            HistoricoSaudeRepository saudeRepository,
            CareerAssessmentRepository assessmentRepository,
            SolicitacaoMentoriaRepository mentoriaRepository,
            InscricaoCursoRepository inscricaoRepository,
            CandidaturaVagaRepository candidaturaRepository,
            UserSessionRepository sessionRepository) {
        this.saudeRepository = saudeRepository;
        this.assessmentRepository = assessmentRepository;
        this.mentoriaRepository = mentoriaRepository;
        this.inscricaoRepository = inscricaoRepository;
        this.candidaturaRepository = candidaturaRepository;
        this.sessionRepository = sessionRepository;
    }

    /** @return total de linhas apagadas nas seis tabelas */
    @Transactional
    public int apagarDadosDe(Collection<Long> userIds) {
        if (userIds == null || userIds.isEmpty()) {
            return 0;
        }
        return saudeRepository.apagarPorUsuarios(userIds)
                + assessmentRepository.apagarPorUsuarios(userIds)
                + mentoriaRepository.apagarPorUsuarios(userIds)
                + inscricaoRepository.apagarPorUsuarios(userIds)
                + candidaturaRepository.apagarPorUsuarios(userIds)
                + sessionRepository.apagarPorUsuarios(userIds);
    }
}
