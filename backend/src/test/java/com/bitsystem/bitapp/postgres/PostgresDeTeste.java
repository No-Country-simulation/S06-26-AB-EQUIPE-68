package com.bitsystem.bitapp.postgres;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.testcontainers.DockerClientFactory;
import org.testcontainers.containers.PostgreSQLContainer;

/**
 * Um único PostgreSQL 16 (a mesma imagem do docker-compose.yml) para toda a
 * execução dos testes, com um banco separado por classe de teste. O container
 * é encerrado pelo Testcontainers quando a JVM termina.
 *
 * Estes testes NÃO são pulados quando o Docker falta: falham, com a mensagem
 * abaixo. Rodar a suíte sem Postgres real foi o que deixou a produção descobrir
 * sozinha o problema do @Lob.
 */
public final class PostgresDeTeste {

    private static PostgreSQLContainer<?> container;

    private PostgresDeTeste() {}

    private static synchronized PostgreSQLContainer<?> container() {
        if (container == null) {
            if (!DockerClientFactory.instance().isDockerAvailable()) {
                throw new IllegalStateException(
                        "Docker indisponível: os testes com PostgreSQL real (Testcontainers) precisam dele e não "
                                + "são pulados em silêncio. Inicie o Docker (docker info deve responder) e rode de novo.");
            }
            PostgreSQLContainer<?> novo = new PostgreSQLContainer<>("postgres:16");
            novo.start();
            container = novo;
        }
        return container;
    }

    /** Cria o banco (se ainda não existir) e devolve a URL JDBC dele. */
    public static synchronized String urlDoBanco(String banco) {
        PostgreSQLContainer<?> pg = container();
        try (Connection c = DriverManager.getConnection(pg.getJdbcUrl(), pg.getUsername(), pg.getPassword());
             Statement st = c.createStatement()) {
            boolean existe;
            try (var rs = st.executeQuery("SELECT 1 FROM pg_database WHERE datname = '" + banco + "'")) {
                existe = rs.next();
            }
            if (!existe) {
                st.execute("CREATE DATABASE " + banco);
            }
        } catch (SQLException e) {
            throw new IllegalStateException("Não foi possível criar o banco de teste '" + banco + "'", e);
        }
        return "jdbc:postgresql://" + pg.getHost() + ":" + pg.getMappedPort(PostgreSQLContainer.POSTGRESQL_PORT) + "/" + banco;
    }

    public static String usuario() {
        return container().getUsername();
    }

    public static String senha() {
        return container().getPassword();
    }

    /** Aponta o datasource do contexto Spring para o banco dado. */
    public static void registrar(DynamicPropertyRegistry registry, String banco) {
        registry.add("spring.datasource.url", () -> urlDoBanco(banco));
        registry.add("spring.datasource.username", PostgresDeTeste::usuario);
        registry.add("spring.datasource.password", PostgresDeTeste::senha);
    }
}
