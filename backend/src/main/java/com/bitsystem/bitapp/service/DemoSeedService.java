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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

/**
 * Seed de demonstração para a vitrine pública (Render): replanta os 6
 * cenários de CVV/tendência a cada boot, já que o H2 in-memory zera a cada
 * sleep/deploy. Só roda com DEMO_SEED=true (default: não faz nada).
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
    private static final String EMAIL_IDEMPOTENCIA = "tendencia1@teste.com";

    private final UserRepository userRepository;
    private final HistoricoSaudeRepository saudeRepository;
    private final VagaRepository vagaRepository;
    private final PasswordEncoder passwordEncoder;
    private final boolean demoSeedAtivo;

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

    public DemoSeedService(
            UserRepository userRepository,
            HistoricoSaudeRepository saudeRepository,
            VagaRepository vagaRepository,
            PasswordEncoder passwordEncoder,
            @Value("${DEMO_SEED:false}") boolean demoSeedAtivo) {
        this.userRepository = userRepository;
        this.saudeRepository = saudeRepository;
        this.vagaRepository = vagaRepository;
        this.passwordEncoder = passwordEncoder;
        this.demoSeedAtivo = demoSeedAtivo;
    }

    @Override
    public void run(String... args) {
        if (!demoSeedAtivo) {
            return;
        }

        if (userRepository.existsByEmail(EMAIL_IDEMPOTENCIA)) {
            log.info("DemoSeedService: dados já presentes, pulando");
            return;
        }

        log.info("DemoSeedService: Iniciando seed de demonstração...");

        LocalDate hoje = LocalDate.now();
        int totalCheckins = 0;

        for (DemoUsuario u : USUARIOS) {
            User user = new User(u.nome(), u.email(), passwordEncoder.encode(SENHA_DEMO));
            user.setCidade(u.cidade());
            user.setNivelProfissional(u.nivel());
            user.setAreaTecnologia(u.area());
            user.setCompetenciasAtuais(u.competencias());
            user = userRepository.save(user);

            List<HistoricoSaude> historico = new ArrayList<>();
            for (int i = 0; i < u.notas().length; i++) {
                int diasAtras = u.notas().length - i; // notas[0] -> D-5 ... notas[4] -> D-1
                LocalDateTime createdAt = hoje.minusDays(diasAtras).atTime(20, 0);
                int nota = u.notas()[i];
                historico.add(HistoricoSaude.builder()
                        .userId(user.getId())
                        .nota(nota)
                        .contexto(null)
                        .derivouCvv(nota <= 3)
                        .createdAt(createdAt)
                        .build());
            }
            saudeRepository.saveAll(historico);
            totalCheckins += historico.size();
        }

        // Vaga âncora determinística: garante à Ana (Dados) um match de 100% em
        // vagas.html contra "Python, SQL, Power BI", eliminando a loteria do seed
        // aleatório. Roda por último (@Order LOWEST), então o catálogo aleatório do
        // VagaSeedService já foi gerado — a âncora apenas se soma a ele.
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
        vagaRepository.save(ancora);
        log.info("DemoSeedService: vaga âncora '{}' ({}) plantada", ancora.getTitulo(), ancora.getTecnologias());

        log.info("DemoSeedService: {} usuários e {} check-ins plantados (histórico de {} a {}, hoje {} vazio)",
                USUARIOS.size(), totalCheckins, hoje.minusDays(5), hoje.minusDays(1), hoje);
    }
}