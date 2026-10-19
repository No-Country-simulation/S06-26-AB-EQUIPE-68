package com.bitsystem.bitapp.integration;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.junit.jupiter.api.Test;
import org.springframework.core.io.ClassPathResource;

/**
 * Orçamento de tempo por tipo de chamada, nos padrões do application.properties (lido do arquivo,
 * não do Environment, para não depender das variáveis exportadas na máquina) contra os tempos do
 * frontend (api.js): o frontend precisa esperar mais que o pior caso do backend.
 */
class TemposDeEsperaTest {

    private static Properties aplicacao() throws IOException {
        Properties props = new Properties();
        try (InputStream in = new ClassPathResource("application.properties").getInputStream()) {
            props.load(in);
        }
        return props;
    }

    /** Valor padrão de "${VARIAVEL:padrao}". */
    private static int padrao(Properties props, String chave) {
        Matcher m = Pattern.compile("\\$\\{[A-Z0-9_]+:(\\d+)\\}").matcher(props.getProperty(chave));
        assertThat(m.matches()).as(chave + " = " + props.getProperty(chave)).isTrue();
        return Integer.parseInt(m.group(1));
    }

    private static int tempoDoFrontend(String constante) throws IOException {
        Path api = Path.of(System.getProperty("basedir", ".")).resolve("../frontend/js/api.js").normalize();
        Matcher m = Pattern.compile("\\b" + constante + "\\s*=\\s*(\\d+)").matcher(Files.readString(api));
        assertThat(m.find()).as(constante + " em api.js").isTrue();
        return Integer.parseInt(m.group(1));
    }

    @Test
    void checkin_piorCasoEntreN8nEGeminiFicaEmAteDozeSegundos_semNovaTentativa() throws IOException {
        Properties p = aplicacao();
        int tentativas = padrao(p, "n8n.max-retries");
        int n8n = padrao(p, "n8n.connect-timeout") + padrao(p, "n8n.mental-health.timeout");
        int gemini = padrao(p, "gemini.connect-timeout") + padrao(p, "gemini.checkin-timeout");

        assertThat(tentativas).isEqualTo(1);
        assertThat(n8n * tentativas + gemini).isLessThanOrEqualTo(12_000);
        assertThat(n8n * tentativas + gemini).isLessThan(tempoDoFrontend("TIMEOUT_CHECKIN_MS"));
    }

    @Test
    void avaliacao_podeEsperarMaisQueOCheckin_ateCercaDeVinteECincoSegundos() throws IOException {
        Properties p = aplicacao();
        int avaliacao = (padrao(p, "n8n.connect-timeout") + padrao(p, "n8n.assessment.timeout")) * padrao(p, "n8n.max-retries");
        int checkin = padrao(p, "n8n.connect-timeout") + padrao(p, "n8n.mental-health.timeout");

        assertThat(avaliacao).isGreaterThan(checkin).isLessThanOrEqualTo(25_000);
        assertThat(avaliacao).isLessThan(tempoDoFrontend("TIMEOUT_AVALIACAO_MS"));
    }

    @Test
    void frontend_checkin20s_avaliacao35s_restoDoApp15s() throws IOException {
        assertThat(tempoDoFrontend("TIMEOUT_CHECKIN_MS")).isEqualTo(20_000);
        assertThat(tempoDoFrontend("TIMEOUT_AVALIACAO_MS")).isEqualTo(35_000);
        assertThat(tempoDoFrontend("TIMEOUT_PADRAO_MS")).isEqualTo(15_000);
    }
}
