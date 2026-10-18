package com.bitsystem.bitapp.integration;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.io.ClassPathResource;

/**
 * Padrões do application.properties para o n8n. Lê o arquivo principal em vez de perguntar ao
 * Environment, que mudaria de resultado se N8N_CHECKIN_TIMEOUT, N8N_MAX_RETRIES ou N8N_WEBHOOK_TOKEN
 * estivessem exportadas na máquina de quem roda os testes.
 */
@SpringBootTest
class ConfiguracaoN8nTest {

    @Autowired
    private N8NMentalHealthClient mentalHealthClient;

    private static Properties aplicacao() throws IOException {
        Properties props = new Properties();
        try (InputStream in = new ClassPathResource("application.properties").getInputStream()) {
            props.load(in);
        }
        return props;
    }

    @Test
    void temposPadraoPorTipoDeChamada_eUmaTentativa() throws IOException {
        assertThat(aplicacao().getProperty("n8n.connect-timeout")).isEqualTo("${N8N_CONNECT_TIMEOUT:1000}");
        assertThat(aplicacao().getProperty("n8n.mental-health.timeout")).isEqualTo("${N8N_CHECKIN_TIMEOUT:5000}");
        assertThat(aplicacao().getProperty("n8n.assessment.timeout")).isEqualTo("${N8N_ASSESSMENT_TIMEOUT:23000}");
        assertThat(aplicacao().getProperty("n8n.max-retries")).isEqualTo("${N8N_MAX_RETRIES:1}");
    }

    @Test
    void tokenDoWebhookVemVazioPorPadrao_eOClienteSobe() throws IOException {
        assertThat(aplicacao().getProperty("n8n.webhook-token")).isEqualTo("${N8N_WEBHOOK_TOKEN:}");
        assertThat(mentalHealthClient).isNotNull();
    }
}
