package com.bitsystem.bitapp.service;

import com.bitsystem.bitapp.domain.User;
import com.bitsystem.bitapp.domain.Vaga;
import com.bitsystem.bitapp.model.HistoricoSaude;
import com.bitsystem.bitapp.repository.HistoricoSaudeRepository;
import com.bitsystem.bitapp.repository.UserRepository;
import com.bitsystem.bitapp.repository.VagaRepository;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

/**
 * Seed de demonstração para a vitrine pública (Render). Só roda com
 * DEMO_SEED=true (default: não faz nada). A cada boot:
 *
 *  1. restaura as 6 contas do cenário de CVV/tendência ao estado original —
 *     apaga o que cada visitante fez nelas (check-ins, avaliações, mentorias,
 *     inscrições, candidaturas, sessões), devolve o perfil e a senha de
 *     demonstração e replanta os check-ins; tudo numa transação;
 *  2. garante a vaga-âncora, sem duplicar;
 *  3. apaga os visitantes vencidos (DemoRetencaoService).
 *
 * Com banco persistente (PostgreSQL), "pular se já existe" deixaria as contas
 * de demonstração acumulando o que cada visitante fizesse. Com H2 em memória
 * o banco nasce vazio e o fluxo cria as contas, como sempre.
 *
 * Histórico de check-ins vai de D-5 a D-1, relativo ao dia do boot; D-0 fica
 * sempre vazio — é o check-in ao vivo de quem abre a demo que fecha (ou não)
 * a janela de tendência (regra em SaudeMentalService.calcularTendenciaSemana).
 */
@Service
@Order(Ordered.LOWEST_PRECEDENCE) // boot: SEMPRE por último — depende das vagas do VagaSeedService
public class DemoSeedService implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(DemoSeedService.class);

    private static final String SENHA_DEMO = "Teste@123";

    private final UserRepository userRepository;
    private final HistoricoSaudeRepository saudeRepository;
    private final VagaRepository vagaRepository;
    private final DadosDoUsuarioService dadosDoUsuario;
    private final DemoRetencaoService retencao;
    private final PasswordEncoder passwordEncoder;
    private final TransactionTemplate transacao;
    private final boolean demoSeedAtivo;

    /** O que o reset fez, para o log do boot e para os testes. Nada de dados pessoais. */
    public record ResultadoReset(int contas, int contasCriadas, int linhasApagadas,
                                 int checkinsPlantados, boolean ancoraCriada) {}

    /**
     * Notas D-5..D-1 (escala CVV v2: 9/7/5/3/1). Matriz aprovada: os "lows"
     * (nota<=3) ficam sempre em D-4..D-1, nunca em D-5 — D-5 sai da janela
     * dos "5 dias mais recentes" assim que o check-in ao vivo de D-0 é
     * gravado, então ele nunca deve carregar sozinho um veredito.
     */
    private record DemoUsuario(
        String nome, String email, String nivel, String area, String cidade, String competencias, int[] notas
    ) {}

    private static final List<DemoUsuario> USUARIOS = List.of(
        new DemoUsuario("Bruno Lima", "tendencia1@teste.com", "Estudante", "Java", "UFSC",
                "Java, Spring Boot, MySQL", new int[]{9, 3, 7, 3, 9}),
        new DemoUsuario("Ana Souza", "tendencia2@teste.com", "Transicao", "Dados", "TRINDADE",
                "Python, SQL, Power BI", new int[]{7, 3, 9, 1, 7}),
        new DemoUsuario("Carla Nunes", "quase1@teste.com", "Graduado", "Web", "CBD_BEIRAMAR",
                "HTML, CSS", new int[]{9, 1, 9, 3, 7}),
        new DemoUsuario("Diego Rocha", "quase2@teste.com", "Estudante", "Ciberseguranca", "INGLESES",
                "Linux, Docker, Python", new int[]{7, 3, 9, 7, 9}),
        new DemoUsuario("Eduardo Alves", "saudavel1@teste.com", "Transicao", "Java", "ESTREITO_CAPOEIRAS",
                "Java, JPA/Hibernate, PostgreSQL", new int[]{9, 7, 9, 7, 9}),
        new DemoUsuario("Fernanda Costa", "saudavel2@teste.com", "Graduado", "Dados", "LAGOA_CONCEICAO",
                "SQL, Excel, Tableau", new int[]{7, 9, 7, 9, 7})
    );

    /** E-mails das contas do cenário: o reset as restaura e a retenção nunca as apaga. */
    public static final Set<String> EMAILS_DO_CENARIO = USUARIOS.stream()
            .map(DemoUsuario::email)
            .collect(Collectors.toUnmodifiableSet());

    public DemoSeedService(
            UserRepository userRepository,
            HistoricoSaudeRepository saudeRepository,
            VagaRepository vagaRepository,
            DadosDoUsuarioService dadosDoUsuario,
            DemoRetencaoService retencao,
            PasswordEncoder passwordEncoder,
            PlatformTransactionManager transactionManager,
            @Value("${DEMO_SEED:false}") boolean demoSeedAtivo) {
        this.userRepository = userRepository;
        this.saudeRepository = saudeRepository;
        this.vagaRepository = vagaRepository;
        this.dadosDoUsuario = dadosDoUsuario;
        this.retencao = retencao;
        this.passwordEncoder = passwordEncoder;
        this.transacao = new TransactionTemplate(transactionManager);
        this.demoSeedAtivo = demoSeedAtivo;
    }

    @Override
    public void run(String... args) {
        if (!demoSeedAtivo) {
            return;
        }

        log.info("DemoSeedService: restaurando o cenário de demonstração...");
        ResultadoReset r = restaurarCenario();
        log.info("DemoSeedService: {} contas do cenário restauradas ({} criadas, {} registros apagados, "
                        + "inclui os check-ins do cenário, replantados em seguida), "
                        + "{} check-ins plantados (D-5 a D-1, hoje vazio), vaga âncora {}",
                r.contas(), r.contasCriadas(), r.linhasApagadas(), r.checkinsPlantados(),
                r.ancoraCriada() ? "plantada" : "já existia");

        retencao.apagarVisitantesAntigos();
    }

    /**
     * Devolve as 6 contas do cenário ao estado original, numa única transação:
     * se algo falhar no meio, nada fica pela metade.
     */
    public ResultadoReset restaurarCenario() {
        return transacao.execute(status -> restaurarNaTransacao());
    }

    private ResultadoReset restaurarNaTransacao() {
        LocalDate hoje = LocalDate.now();
        int criadas = 0;

        List<User> contas = new ArrayList<>();
        for (DemoUsuario u : USUARIOS) {
            User user = userRepository.findByEmail(u.email()).orElse(null);
            if (user == null) {
                user = new User(u.nome(), u.email(), null);
                criadas++;
            }
            user.setNome(u.nome());
            user.setPasswordHash(passwordEncoder.encode(SENHA_DEMO));
            user.setCidade(u.cidade());
            user.setNivelProfissional(u.nivel());
            user.setAreaTecnologia(u.area());
            user.setCompetenciasAtuais(u.competencias());
            // campos que o cadastro do cenário nunca preenche: voltam ao valor de uma conta nova
            user.setWhatsapp(null);
            user.setLatitude(null);
            user.setLongitude(null);
            user.setIdiomaPreferido("pt");
            contas.add(userRepository.save(user));
        }

        List<Long> ids = contas.stream().map(User::getId).toList();
        int linhasApagadas = dadosDoUsuario.apagarDadosDe(ids);

        int totalCheckins = 0;
        for (int c = 0; c < USUARIOS.size(); c++) {
            DemoUsuario u = USUARIOS.get(c);
            Long userId = ids.get(c);
            List<HistoricoSaude> historico = new ArrayList<>();
            for (int i = 0; i < u.notas().length; i++) {
                int diasAtras = u.notas().length - i; // notas[0] -> D-5 ... notas[4] -> D-1
                LocalDateTime createdAt = hoje.minusDays(diasAtras).atTime(20, 0);
                int nota = u.notas()[i];
                historico.add(HistoricoSaude.builder()
                        .userId(userId)
                        .nota(nota)
                        .contexto(null)
                        .derivouCvv(nota <= 3)
                        .createdAt(createdAt)
                        .build());
            }
            saudeRepository.saveAll(historico);
            totalCheckins += historico.size();
        }

        boolean ancoraCriada = garantirVagaAncora();
        return new ResultadoReset(contas.size(), criadas, linhasApagadas, totalCheckins, ancoraCriada);
    }

    /**
     * Vaga âncora determinística: garante à Ana (Dados) um match de 100% em
     * vagas.html contra "Python, SQL, Power BI", eliminando a loteria do seed
     * aleatório. Roda por último (@Order LOWEST), então o catálogo aleatório do
     * VagaSeedService já foi gerado — a âncora apenas se soma a ele. Se já
     * existir (banco persistente, boot seguinte), não cria outra.
     *
     * @return true se criou a vaga neste boot
     */
    private boolean garantirVagaAncora() {
        Vaga ancora = new Vaga(
                "Engenheiro de Dados",
                "DataFlow Analytics",
                "TRINDADE",
                "Vaga para profissional de dados Pleno. Responsável por análise, "
                        + "modelagem e visualização de dados, auxiliando na tomada de "
                        + "decisão baseada em evidências.",
                "Pleno",
                "Dados",
                "CLT",
                "R$ 5.000 - R$ 8.000",
                "Python, SQL, Power BI",
                "#",
                "Híbrido");

        boolean jaExiste = vagaRepository
                .findByTituloAndEmpresaAndRegiaoAndNivelAndAreaAndTipoContratoAndSalario(
                        ancora.getTitulo(), ancora.getEmpresa(), ancora.getRegiao(), ancora.getNivel(),
                        ancora.getArea(), ancora.getTipoContrato(), ancora.getSalario())
                .stream()
                .anyMatch(v -> ancora.getTecnologias().equals(v.getTecnologias()));
        if (jaExiste) {
            return false;
        }
        vagaRepository.save(ancora);
        return true;
    }
}
