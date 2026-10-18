package com.bitsystem.bitapp.service;

import static org.assertj.core.api.Assertions.assertThat;

import com.bitsystem.bitapp.domain.CandidaturaVaga;
import com.bitsystem.bitapp.domain.CareerAssessment;
import com.bitsystem.bitapp.domain.InscricaoCurso;
import com.bitsystem.bitapp.domain.SolicitacaoMentoria;
import com.bitsystem.bitapp.domain.User;
import com.bitsystem.bitapp.domain.UserSession;
import com.bitsystem.bitapp.model.HistoricoSaude;
import com.bitsystem.bitapp.repository.CandidaturaVagaRepository;
import com.bitsystem.bitapp.repository.CareerAssessmentRepository;
import com.bitsystem.bitapp.repository.CursoRepository;
import com.bitsystem.bitapp.repository.HistoricoSaudeRepository;
import com.bitsystem.bitapp.repository.InfraestruturaRedeRepository;
import com.bitsystem.bitapp.repository.InscricaoCursoRepository;
import com.bitsystem.bitapp.repository.MentorRepository;
import com.bitsystem.bitapp.repository.SolicitacaoMentoriaRepository;
import com.bitsystem.bitapp.repository.UserRepository;
import com.bitsystem.bitapp.repository.UserSessionRepository;
import com.bitsystem.bitapp.repository.VagaRepository;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.PlatformTransactionManager;

/**
 * Cenário de demonstração com banco persistente: o que o DemoSeedService faz
 * em cada boot (reset das 6 contas, vaga-âncora sem duplicar) e a retenção de
 * visitantes. A mesma bateria roda em H2 (DemoSeedH2Test) e em PostgreSQL real
 * (DemoSeedPostgresTest, via Testcontainers). Cada subclasse sobe o contexto com
 * DEMO_SEED=true, então o "primeiro boot" já plantou o cenário.
 */
abstract class DemoSeedBaseTest {

    static final String SENHA_DEMO = "Teste@123";

    @Autowired DemoSeedService demoSeedService;
    @Autowired DemoRetencaoService retencaoService;
    @Autowired DadosDoUsuarioService dadosDoUsuario;
    @Autowired PasswordEncoder passwordEncoder;
    @Autowired JdbcTemplate jdbc;
    @Autowired PlatformTransactionManager transactionManager;

    @Autowired UserRepository userRepository;
    @Autowired HistoricoSaudeRepository saudeRepository;
    @Autowired CareerAssessmentRepository assessmentRepository;
    @Autowired SolicitacaoMentoriaRepository mentoriaRepository;
    @Autowired InscricaoCursoRepository inscricaoRepository;
    @Autowired CandidaturaVagaRepository candidaturaRepository;
    @Autowired UserSessionRepository sessionRepository;
    @Autowired VagaRepository vagaRepository;
    @Autowired CursoRepository cursoRepository;
    @Autowired MentorRepository mentorRepository;
    @Autowired InfraestruturaRedeRepository antenaRepository;

    @Autowired VagaSeedService vagaSeedService;
    @Autowired CursoSeedService cursoSeedService;
    @Autowired MentorSeedService mentorSeedService;

    // ── utilitários ─────────────────────────────────────────────────────────

    User conta(String email) {
        return userRepository.findByEmail(email).orElseThrow();
    }

    /** Dá a uma conta um pouco de tudo que um visitante pode produzir na demo. */
    void encherDeUso(Long userId) {
        saudeRepository.save(HistoricoSaude.builder()
                .userId(userId).nota(1).contexto("check-in de teste").derivouCvv(true).build());
        assessmentRepository.save(new CareerAssessment(userId, 80, "Pleno", "pontos", "gaps", "plano"));
        mentoriaRepository.save(SolicitacaoMentoria.builder()
                .usuarioId(userId).mentorId(1L).areaSolicitada("Java").mensagem("oi").build());
        inscricaoRepository.save(InscricaoCurso.builder().usuarioId(userId).cursoId(1L).build());
        candidaturaRepository.save(CandidaturaVaga.builder()
                .usuarioId(userId).vagaId(1L).nome("n").email("e@e.com").mensagem("m").build());
        UserSession sessao = new UserSession();
        sessao.setUsuarioId(userId);
        sessao.setToken(UUID.randomUUID().toString());
        sessao.setRefreshToken(UUID.randomUUID().toString());
        sessao.setExpiresAt(LocalDateTime.now().plusDays(1));
        sessionRepository.save(sessao);
    }

    long itensDe(Long userId) {
        return saudeRepository.findByUserIdOrderByCreatedAtDesc(userId).size()
                + assessmentRepository.findByUsuarioIdOrderByCreatedAtDesc(userId).size()
                + mentoriaRepository.findByUsuarioIdOrderByCreatedAtDesc(userId).size()
                + inscricaoRepository.findByUsuarioIdOrderByDataInscricaoDesc(userId).size()
                + candidaturaRepository.findByUsuarioIdOrderByDataEnvioDesc(userId).size()
                + sessionRepository.findByUsuarioId(userId).size();
    }

    User criarVisitante(String prefixo, int diasDeCadastro) {
        User v = new User("Visitante " + prefixo, prefixo + "-" + UUID.randomUUID() + "@visitante.test",
                passwordEncoder.encode("outra-senha"));
        v = userRepository.save(v);
        // created_at é updatable=false: para "envelhecer" a conta, só por SQL
        jdbc.update("UPDATE users SET created_at = ? WHERE id = ?",
                LocalDateTime.now().minusDays(diasDeCadastro), v.getId());
        return v;
    }

    long ancoras() {
        return vagaRepository.findAll().stream()
                .filter(v -> "Engenheiro de Dados".equals(v.getTitulo())
                        && "DataFlow Analytics".equals(v.getEmpresa())
                        && "TRINDADE".equals(v.getRegiao())
                        && "Python, SQL, Power BI".equals(v.getTecnologias())
                        && "CLT".equals(v.getTipoContrato())
                        && "R$ 5.000 - R$ 8.000".equals(v.getSalario()))
                .count();
    }

    // ── item A: reset das contas de demonstração ───────────────────────────

    @Test
    void primeiroBoot_plantaOCenarioCompleto() {
        for (String email : DemoSeedService.EMAILS_DO_CENARIO) {
            User u = conta(email);
            assertThat(saudeRepository.findByUserIdOrderByCreatedAtDesc(u.getId())).hasSize(5);
            assertThat(passwordEncoder.matches(SENHA_DEMO, u.getPasswordHash())).isTrue();
        }
        assertThat(DemoSeedService.EMAILS_DO_CENARIO).hasSize(6);
        assertThat(ancoras()).isEqualTo(1);
    }

    @Test
    void reset_devolveContaSujaAoEstadoDoCenario() {
        User bruno = conta("tendencia1@teste.com");
        Long id = bruno.getId();

        // visitante "suja" a conta: perfil, senha e uso
        bruno.setNome("Outro Nome");
        bruno.setCidade("TRINDADE");
        bruno.setNivelProfissional("Graduado");
        bruno.setAreaTecnologia("Web");
        bruno.setCompetenciasAtuais("Cobol");
        bruno.setWhatsapp("48999990000");
        bruno.setLatitude(-27.1);
        bruno.setLongitude(-48.1);
        bruno.setIdiomaPreferido("es");
        bruno.setPasswordHash(passwordEncoder.encode("senha-trocada"));
        userRepository.save(bruno);
        encherDeUso(id);
        assertThat(itensDe(id)).isGreaterThan(5); // 5 do cenário + o que foi acrescentado

        DemoSeedService.ResultadoReset r = demoSeedService.restaurarCenario();

        User depois = conta("tendencia1@teste.com");
        assertThat(depois.getId()).isEqualTo(id);
        assertThat(depois.getNome()).isEqualTo("Bruno Lima");
        assertThat(depois.getCidade()).isEqualTo("UFSC");
        assertThat(depois.getNivelProfissional()).isEqualTo("Estudante");
        assertThat(depois.getAreaTecnologia()).isEqualTo("Java");
        assertThat(depois.getCompetenciasAtuais()).isEqualTo("Java, Spring Boot, MySQL");
        assertThat(depois.getWhatsapp()).isNull();
        assertThat(depois.getLatitude()).isNull();
        assertThat(depois.getLongitude()).isNull();
        assertThat(depois.getIdiomaPreferido()).isEqualTo("pt");
        assertThat(passwordEncoder.matches(SENHA_DEMO, depois.getPasswordHash())).isTrue();

        assertThat(assessmentRepository.findByUsuarioIdOrderByCreatedAtDesc(id)).isEmpty();
        assertThat(mentoriaRepository.findByUsuarioIdOrderByCreatedAtDesc(id)).isEmpty();
        assertThat(inscricaoRepository.findByUsuarioIdOrderByDataInscricaoDesc(id)).isEmpty();
        assertThat(candidaturaRepository.findByUsuarioIdOrderByDataEnvioDesc(id)).isEmpty();
        assertThat(sessionRepository.findByUsuarioId(id)).isEmpty();

        // check-ins: exatamente os 5 do cenário, D-5..D-1 às 20:00, notas 9/3/7/3/9
        List<HistoricoSaude> checkins = saudeRepository.findByUserIdOrderByCreatedAtDesc(id);
        assertThat(checkins).hasSize(5);
        int[] notas = {9, 3, 7, 3, 9};
        LocalDate hoje = LocalDate.now();
        for (int i = 0; i < 5; i++) {
            HistoricoSaude h = checkins.get(4 - i); // a lista vem do mais novo para o mais velho
            assertThat(h.getNota()).isEqualTo(notas[i]);
            assertThat(h.getCreatedAt()).isEqualTo(hoje.minusDays(5 - i).atTime(20, 0));
            assertThat(h.getDerivouCvv()).isEqualTo(notas[i] <= 3);
        }

        assertThat(r.contas()).isEqualTo(6);
        assertThat(r.contasCriadas()).isZero();
        assertThat(r.linhasApagadas()).isGreaterThanOrEqualTo(5 + 6); // 5 antigos + 6 acrescentados
        assertThat(r.checkinsPlantados()).isEqualTo(30);
        assertThat(r.ancoraCriada()).isFalse();
    }

    @Test
    void reset_naoTocaEmContaDeVisitante() {
        User visitante = criarVisitante("reset", 1);
        encherDeUso(visitante.getId());
        long antes = itensDe(visitante.getId());

        demoSeedService.restaurarCenario();

        assertThat(userRepository.findById(visitante.getId())).isPresent();
        assertThat(itensDe(visitante.getId())).isEqualTo(antes);
    }

    @Test
    void reset_recriaContaDoCenarioApagada() {
        User carla = conta("quase1@teste.com");
        Long id = carla.getId();
        dadosDoUsuario.apagarDadosDe(List.of(id));
        userRepository.deleteById(id);
        assertThat(userRepository.existsByEmail("quase1@teste.com")).isFalse();

        DemoSeedService.ResultadoReset r = demoSeedService.restaurarCenario();

        assertThat(r.contasCriadas()).isEqualTo(1);
        User nova = conta("quase1@teste.com");
        assertThat(nova.getNome()).isEqualTo("Carla Nunes");
        assertThat(saudeRepository.findByUserIdOrderByCreatedAtDesc(nova.getId())).hasSize(5);
    }

    @Test
    void reset_naoDuplicaAVagaAncoraEmBootsSeguintes() {
        long vagasAntes = vagaRepository.count();
        assertThat(ancoras()).isEqualTo(1);

        demoSeedService.run();
        demoSeedService.run();

        assertThat(ancoras()).isEqualTo(1);
        assertThat(vagaRepository.count()).isEqualTo(vagasAntes);
    }

    @Test
    void catalogosNaoDuplicamEmBootsSeguintes() {
        long vagas = vagaRepository.count();
        long cursos = cursoRepository.count();
        long mentores = mentorRepository.count();
        long antenas = antenaRepository.count();
        assertThat(vagas).isPositive();
        assertThat(cursos).isPositive();
        assertThat(mentores).isPositive();

        vagaSeedService.run();
        cursoSeedService.run();
        mentorSeedService.run();

        assertThat(vagaRepository.count()).isEqualTo(vagas);
        assertThat(cursoRepository.count()).isEqualTo(cursos);
        assertThat(mentorRepository.count()).isEqualTo(mentores);
        assertThat(antenaRepository.count()).isEqualTo(antenas);
    }

    // ── item B: retenção de visitantes ─────────────────────────────────────

    @Test
    void retencao_apagaVisitanteDe31DiasComTudoQueEraDele_eMantemODe29() {
        User velho = criarVisitante("velho", 31);
        User recente = criarVisitante("recente", 29);
        encherDeUso(velho.getId());
        encherDeUso(recente.getId());
        long itensDoRecente = itensDe(recente.getId());
        assertThat(itensDe(velho.getId())).isEqualTo(6);

        int apagadas = retencaoService.apagarVisitantesAntigos();

        assertThat(apagadas).isGreaterThanOrEqualTo(1);
        assertThat(userRepository.findById(velho.getId())).isEmpty();
        assertThat(itensDe(velho.getId())).isZero();
        assertThat(userRepository.findById(recente.getId())).isPresent();
        assertThat(itensDe(recente.getId())).isEqualTo(itensDoRecente);
    }

    @Test
    void retencao_nuncaApagaContaDoCenario_mesmoAntiga() {
        for (String email : DemoSeedService.EMAILS_DO_CENARIO) {
            jdbc.update("UPDATE users SET created_at = ? WHERE email = ?",
                    LocalDateTime.now().minusDays(400), email);
        }

        retencaoService.apagarVisitantesAntigos();

        for (String email : DemoSeedService.EMAILS_DO_CENARIO) {
            User u = conta(email);
            assertThat(saudeRepository.findByUserIdOrderByCreatedAtDesc(u.getId())).hasSize(5);
        }
    }

    @Test
    void retencao_menorQueUm_desligaALimpeza() {
        User velho = criarVisitante("velho-desligado", 500);
        DemoRetencaoService desligada =
                new DemoRetencaoService(userRepository, dadosDoUsuario, transactionManager, 0);

        assertThat(desligada.apagarVisitantesAntigos()).isZero();

        assertThat(userRepository.findById(velho.getId())).isPresent();
    }

    @Test
    void retencao_respeitaOPrazoConfigurado() {
        User com10Dias = criarVisitante("dez-dias", 10);
        User com3Dias = criarVisitante("tres-dias", 3);
        DemoRetencaoService prazoCurto =
                new DemoRetencaoService(userRepository, dadosDoUsuario, transactionManager, 7);

        prazoCurto.apagarVisitantesAntigos();

        assertThat(userRepository.findById(com10Dias.getId())).isEmpty();
        assertThat(userRepository.findById(com3Dias.getId())).isPresent();
    }
}
