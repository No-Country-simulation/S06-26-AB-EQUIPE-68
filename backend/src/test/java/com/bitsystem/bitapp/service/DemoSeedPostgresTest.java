package com.bitsystem.bitapp.service;

import com.bitsystem.bitapp.postgres.PostgresDeTeste;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;

/**
 * A mesma bateria do cenário de demonstração (reset das 6 contas, vaga-âncora,
 * catálogos sem duplicar, retenção de visitantes), agora em PostgreSQL 16 real.
 */
@SpringBootTest(properties = "DEMO_SEED=true")
class DemoSeedPostgresTest extends DemoSeedBaseTest {

    @DynamicPropertySource
    static void postgres(DynamicPropertyRegistry registry) {
        PostgresDeTeste.registrar(registry, "demoseed");
    }
}
