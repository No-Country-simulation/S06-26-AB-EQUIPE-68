package com.bitsystem.bitapp.service;

import com.bitsystem.bitapp.repository.UserRepository;
import java.time.LocalDateTime;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

/**
 * Retenção das contas de visitantes da demonstração: no boot (chamado pelo
 * DemoSeedService, ou seja, só com DEMO_SEED=true) apaga as contas que não são
 * do cenário e foram criadas há mais de DEMO_RETENCAO_DIAS dias (padrão 30),
 * junto com tudo o que pertence a elas.
 *
 * O aviso do cadastro (frontend/js/i18n.js, chave cadastro.avisoDemo) cita o
 * mesmo prazo; um teste confere que o número do texto bate com o padrão daqui.
 *
 * Valor menor que 1 desliga a retenção — nunca apaga "tudo".
 * O log registra só quantas contas foram apagadas, nunca quais.
 */
@Service
public class DemoRetencaoService {

    private static final Logger log = LoggerFactory.getLogger(DemoRetencaoService.class);

    private final UserRepository userRepository;
    private final DadosDoUsuarioService dadosDoUsuario;
    private final TransactionTemplate transacao;
    private final int retencaoDias;

    public DemoRetencaoService(
            UserRepository userRepository,
            DadosDoUsuarioService dadosDoUsuario,
            PlatformTransactionManager transactionManager,
            @Value("${demo.retencao-dias:30}") int retencaoDias) {
        this.userRepository = userRepository;
        this.dadosDoUsuario = dadosDoUsuario;
        this.transacao = new TransactionTemplate(transactionManager);
        this.retencaoDias = retencaoDias;
    }

    /**
     * Apaga os visitantes vencidos numa única transação. Falha aqui não derruba
     * o boot: a limpeza é manutenção, e o erro fica registrado (sem dados pessoais).
     *
     * @return quantas contas foram apagadas
     */
    public int apagarVisitantesAntigos() {
        if (retencaoDias < 1) {
            log.info("DemoRetencaoService: retenção desligada (DEMO_RETENCAO_DIAS={})", retencaoDias);
            return 0;
        }
        try {
            Integer apagadas = transacao.execute(status -> apagarNaTransacao());
            int total = apagadas == null ? 0 : apagadas;
            log.info("DemoRetencaoService: {} contas de visitantes com mais de {} dias apagadas",
                    total, retencaoDias);
            return total;
        } catch (RuntimeException e) {
            log.error("DemoRetencaoService: não foi possível apagar os visitantes vencidos ({})",
                    e.getClass().getSimpleName());
            return 0;
        }
    }

    private int apagarNaTransacao() {
        LocalDateTime limite = LocalDateTime.now().minusDays(retencaoDias);
        List<Long> ids = userRepository.idsCriadosAntesDe(limite, DemoSeedService.EMAILS_DO_CENARIO);
        if (ids.isEmpty()) {
            return 0;
        }
        dadosDoUsuario.apagarDadosDe(ids);
        userRepository.deleteAllByIdInBatch(ids);
        return ids.size();
    }
}
