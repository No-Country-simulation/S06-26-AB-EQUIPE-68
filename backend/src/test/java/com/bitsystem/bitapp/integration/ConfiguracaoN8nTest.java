package com.bitsystem.bitapp.integration;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.env.Environment;

/** Padrões do application.properties para o n8n (sem variáveis de ambiente definidas). */
@SpringBootTest
class ConfiguracaoN8nTest {

    @Autowired
    private Environment env;

    @Autowired
    private N8NMentalHealthClient mentalHealthClient;

    @Test
    void timeoutPadraoEh15SegundosEUmaTentativa() {
        assertThat(env.getProperty("n8n.timeout", Integer.class)).isEqualTo(15000);
        assertThat(env.getProperty("n8n.max-retries", Integer.class)).isEqualTo(1);
    }

    @Test
    void tokenDoWebhookVemVazioPorPadrao_eOClienteSobe() {
        assertThat(env.getProperty("n8n.webhook-token")).isEmpty();
        assertThat(mentalHealthClient).isNotNull();
    }
}
