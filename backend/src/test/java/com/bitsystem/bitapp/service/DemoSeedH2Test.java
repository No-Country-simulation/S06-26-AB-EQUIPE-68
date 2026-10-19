package com.bitsystem.bitapp.service;

import org.springframework.boot.test.context.SpringBootTest;

/**
 * Bateria do cenário de demonstração em H2 (o banco padrão do desenvolvimento).
 * Usa um banco em memória só dele, para o cenário de demonstração não aparecer
 * nos demais testes, que compartilham o H2 padrão.
 */
@SpringBootTest(properties = {
        "DEMO_SEED=true",
        "spring.datasource.url=jdbc:h2:mem:demoseedh2;DB_CLOSE_DELAY=-1;MODE=MySQL"
})
class DemoSeedH2Test extends DemoSeedBaseTest {
}
