package com.bitsystem.bitapp.postgres;

import static org.assertj.core.api.Assertions.assertThat;

import com.bitsystem.bitapp.BitappApplication;
import com.bitsystem.bitapp.domain.CareerAssessment;
import com.bitsystem.bitapp.model.HistoricoSaude;
import com.bitsystem.bitapp.repository.CareerAssessmentRepository;
import com.bitsystem.bitapp.repository.HistoricoSaudeRepository;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.WebApplicationType;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;

/**
 * A aplicação contra PostgreSQL 16 real: sobe, cria o esquema e devolve o que
 * gravou depois de o contexto ser fechado e reaberto.
 */
@SpringBootTest
class PostgresRealTest {

    @Autowired JdbcTemplate jdbc;

    @DynamicPropertySource
    static void postgres(DynamicPropertyRegistry registry) {
        PostgresDeTeste.registrar(registry, "esquema");
    }

    @Test
    void aplicacaoSobeContraPostgresECriaOEsquema() {
        String versao = jdbc.queryForObject("SELECT version()", String.class);
        assertThat(versao).startsWith("PostgreSQL 16");

        List<String> tabelas = jdbc.queryForList(
                "SELECT table_name FROM information_schema.tables WHERE table_schema = 'public'", String.class);
        assertThat(tabelas).contains("users", "user_sessions", "historico_saude", "career_assessments",
                "solicitacoes_mentoria", "inscricao_curso", "candidatura_vaga", "vagas", "cursos", "mentores");

        // texto longo é TEXT, nunca Large Object (oid): o bug do @Lob só aparecia em Postgres real
        List<String> tipos = jdbc.queryForList(
                "SELECT data_type FROM information_schema.columns WHERE table_name = 'career_assessments' "
                        + "AND column_name IN ('pontos_fortes', 'gaps', 'plano_desenvolvimento')", String.class);
        assertThat(tipos).hasSize(3).containsOnly("text");
    }

    @Test
    void dadoGravadoVoltaDepoisDeFecharEReabrirOContexto() throws Exception {
        String url = PostgresDeTeste.urlDoBanco("persistencia");
        String[] args = {
                "--spring.datasource.url=" + url,
                "--spring.datasource.username=" + PostgresDeTeste.usuario(),
                "--spring.datasource.password=" + PostgresDeTeste.senha(),
                "--DEMO_SEED=false",
                "--visent.enabled=false"
        };
        long userId = 987654L;
        String marca = "check-in-" + UUID.randomUUID();
        String textoLongo = "ponto forte ".repeat(800); // ~9.600 caracteres, bem acima de VARCHAR(255)

        try (ConfigurableApplicationContext primeiro = abrir(args)) {
            primeiro.getBean(HistoricoSaudeRepository.class).save(HistoricoSaude.builder()
                    .userId(userId).nota(7).contexto(marca).derivouCvv(false).build());
            primeiro.getBean(CareerAssessmentRepository.class)
                    .save(new CareerAssessment(userId, 77, "Pleno", textoLongo, "gaps", "plano"));
        }

        // contexto novo, mesma base: nada ficou só em memória
        try (ConfigurableApplicationContext segundo = abrir(args)) {
            List<HistoricoSaude> checkins = segundo.getBean(HistoricoSaudeRepository.class)
                    .findByUserIdOrderByCreatedAtDesc(userId);
            assertThat(checkins).extracting(HistoricoSaude::getContexto).contains(marca);

            List<CareerAssessment> avaliacoes = segundo.getBean(CareerAssessmentRepository.class)
                    .findByUsuarioIdOrderByCreatedAtDesc(userId);
            assertThat(avaliacoes).extracting(CareerAssessment::getPontosFortes).contains(textoLongo);
        }

        // e uma conexão nova, de fora da aplicação, enxerga a mesma linha
        try (Connection c = DriverManager.getConnection(url, PostgresDeTeste.usuario(), PostgresDeTeste.senha());
             PreparedStatement ps = c.prepareStatement("SELECT contexto FROM historico_saude WHERE user_id = ?")) {
            ps.setLong(1, userId);
            List<String> contextos = new ArrayList<>();
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    contextos.add(rs.getString(1));
                }
            }
            assertThat(contextos).contains(marca);
        }
    }

    private static ConfigurableApplicationContext abrir(String[] args) {
        return new SpringApplicationBuilder(BitappApplication.class)
                .web(WebApplicationType.NONE)
                .run(args);
    }
}
