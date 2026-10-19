package com.bitsystem.bitapp.config;

import static org.assertj.core.api.Assertions.assertThat;

import com.bitsystem.bitapp.BitappApplication;
import com.zaxxer.hikari.HikariDataSource;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.springframework.boot.WebApplicationType;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.core.env.StandardEnvironment;
import org.springframework.core.env.SystemEnvironmentPropertySource;

/**
 * O pool não pode guardar conexões ociosas: o PostgreSQL do plano gratuito do
 * Neon só dorme se ficar 5 minutos sem nenhuma conexão. Também confere se os
 * nomes de variável que já existem no Render são lidos pelo Spring.
 */
class PoolDeConexoesTest {

    @Test
    void padroes_naoGuardamConexaoOciosa_eTemTetoPequeno() {
        // Ambiente simulado e vazio: o resultado não muda se a máquina tiver DB_POOL_* ou
        // SPRING_DATASOURCE_HIKARI_* exportadas.
        HikariDataSource pool = subirComAmbiente(Map.of());

        assertThat(pool.getMinimumIdle()).isZero();
        assertThat(pool.getIdleTimeout()).isEqualTo(60_000L);
        assertThat(pool.getMaximumPoolSize()).isEqualTo(5);
    }

    /** Sobe a aplicação com variáveis de ambiente simuladas (mesma classe e nome da fonte real do Spring). */
    private static HikariDataSource subirComAmbiente(Map<String, Object> ambiente, String... args) {
        StandardEnvironment env = new StandardEnvironment();
        env.getPropertySources().replace(
                StandardEnvironment.SYSTEM_ENVIRONMENT_PROPERTY_SOURCE_NAME,
                new SystemEnvironmentPropertySource(
                        StandardEnvironment.SYSTEM_ENVIRONMENT_PROPERTY_SOURCE_NAME, ambiente));
        try (ConfigurableApplicationContext ctx = new SpringApplicationBuilder(BitappApplication.class)
                .web(WebApplicationType.NONE)
                .environment(env)
                .properties("spring.datasource.url=jdbc:h2:mem:pooltest;DB_CLOSE_DELAY=-1;MODE=MySQL",
                        "visent.enabled=false")
                .run(args)) {
            HikariDataSource origem = ctx.getBean(HikariDataSource.class);
            // copia os valores antes de o contexto fechar
            HikariDataSource copia = new HikariDataSource();
            copia.setMinimumIdle(origem.getMinimumIdle());
            copia.setMaximumPoolSize(origem.getMaximumPoolSize());
            copia.setIdleTimeout(origem.getIdleTimeout());
            return copia;
        }
    }

    @Test
    void variaveisDoRender_SPRING_DATASOURCE_HIKARI_sao_lidas_e_vencem_os_padroes() {
        HikariDataSource pool = subirComAmbiente(Map.of(
                "SPRING_DATASOURCE_HIKARI_MAXIMUM_POOL_SIZE", "3",
                "SPRING_DATASOURCE_HIKARI_MINIMUM_IDLE", "2"));

        assertThat(pool.getMaximumPoolSize()).isEqualTo(3);
        assertThat(pool.getMinimumIdle()).isEqualTo(2);
        assertThat(pool.getIdleTimeout()).isEqualTo(60_000L); // não mexida: continua o padrão
    }

    @Test
    void variaveisNovasDB_POOL_ajustamOPool() {
        HikariDataSource pool = subirComAmbiente(Map.of(
                "DB_POOL_MAX_SIZE", "4",
                "DB_POOL_MIN_IDLE", "1",
                "DB_POOL_IDLE_TIMEOUT_MS", "30000"));

        assertThat(pool.getMaximumPoolSize()).isEqualTo(4);
        assertThat(pool.getMinimumIdle()).isEqualTo(1);
        assertThat(pool.getIdleTimeout()).isEqualTo(30_000L);
    }

    @Test
    void variavelSPRING_vence_a_DB_POOL_quando_as_duas_existem() {
        HikariDataSource pool = subirComAmbiente(Map.of(
                "DB_POOL_MAX_SIZE", "4",
                "SPRING_DATASOURCE_HIKARI_MAXIMUM_POOL_SIZE", "2"));

        assertThat(pool.getMaximumPoolSize()).isEqualTo(2);
    }
}
