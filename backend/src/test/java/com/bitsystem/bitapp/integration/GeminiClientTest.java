package com.bitsystem.bitapp.integration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.sun.net.httpserver.HttpServer;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.client.ResourceAccessException;

/**
 * Os tempos do GeminiClient valem de verdade: antes, o campo gemini.timeout era lido e nunca
 * aplicado, e uma chamada travada esperava para sempre. Servidor local que demora para responder.
 */
class GeminiClientTest {

    private static final String RESPOSTA = "{\"candidates\":[{\"content\":{\"parts\":[{\"text\":\"oi\"}]}}]}";

    private HttpServer servidor;

    @BeforeEach
    void subirGeminiLento() throws IOException {
        servidor = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
        servidor.createContext("/", troca -> {
            try {
                Thread.sleep(2000);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
            byte[] corpo = RESPOSTA.getBytes(StandardCharsets.UTF_8);
            troca.getResponseHeaders().add("Content-Type", "application/json");
            troca.sendResponseHeaders(200, corpo.length);
            troca.getResponseBody().write(corpo);
            troca.close();
        });
        servidor.start();
    }

    @AfterEach
    void derrubar() {
        servidor.stop(0);
    }

    private GeminiClient cliente(int checkinMs, int padraoMs) {
        GeminiClient cliente = new GeminiClient(1000, checkinMs, padraoMs);
        ReflectionTestUtils.setField(cliente, "apiKey", "chave-so-de-teste");
        ReflectionTestUtils.setField(cliente, "model", "modelo");
        ReflectionTestUtils.setField(cliente, "endpoint", "http://127.0.0.1:" + servidor.getAddress().getPort());
        ReflectionTestUtils.setField(cliente, "temperature", 0.7);
        ReflectionTestUtils.setField(cliente, "candidateCount", 1);
        ReflectionTestUtils.setField(cliente, "maxOutputTokens", 256);
        return cliente;
    }

    @Test
    void checkin_desisteNoTempoCurto_eAsDemaisChamadasEsperamMais() throws Exception {
        GeminiClient cliente = cliente(300, 6000);

        long inicio = System.nanoTime();
        assertThrows(ResourceAccessException.class, () -> cliente.generateContent("prompt", GeminiClient.Espera.CHECKIN));
        long ms = (System.nanoTime() - inicio) / 1_000_000;
        assertThat(ms).as("check-in desistiu antes de o Gemini lento (2 s) responder").isLessThan(1500);

        assertThat(cliente.generateContent("prompt")).isEqualTo("oi");
    }
}
