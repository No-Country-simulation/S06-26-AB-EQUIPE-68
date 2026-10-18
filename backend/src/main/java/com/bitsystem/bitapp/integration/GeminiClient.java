package com.bitsystem.bitapp.integration;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.time.Duration;
import java.util.List;
import java.util.Map;

@Component
public class GeminiClient {

    private static final Logger log = LoggerFactory.getLogger(GeminiClient.class);

    @Value("${gemini.api-key:}")
    private String apiKey;

    @Value("${gemini.model:gemini-3.5-flash-lite}")
    private String model;

    @Value("${gemini.endpoint:https://generativelanguage.googleapis.com/v1beta/models}")
    private String endpoint;

    @Value("${gemini.temperature:0.7}")
    private double temperature;

    @Value("${gemini.candidate-count:1}")
    private int candidateCount;

    @Value("${gemini.max-output-tokens:2048}")
    private int maxOutputTokens;

    /** Tempo de espera da chamada: o check-in é curto (há um painel de segurança esperando), o resto pode esperar mais. */
    public enum Espera { CHECKIN, PADRAO }

    private final RestTemplate restTemplateCheckin;
    private final RestTemplate restTemplatePadrao;

    public GeminiClient(
            @Value("${gemini.connect-timeout:1000}") int connectTimeout,
            @Value("${gemini.checkin-timeout:5000}") int checkinTimeout,
            @Value("${gemini.timeout:10000}") int padraoTimeout) {
        this.restTemplateCheckin = comTempos(connectTimeout, checkinTimeout);
        this.restTemplatePadrao = comTempos(connectTimeout, padraoTimeout);
    }

    private static RestTemplate comTempos(int connectTimeoutMs, int readTimeoutMs) {
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(Duration.ofMillis(connectTimeoutMs));
        factory.setReadTimeout(Duration.ofMillis(readTimeoutMs));
        return new RestTemplate(factory);
    }

    /**
     * Envia um prompt para a API do Gemini e retorna o texto da resposta.
     *
     * @param prompt o texto completo a enviar
     * @return texto gerado pelo modelo
     * @throws Exception se a API retornar erro ou a chave nao estiver configurada
     */
    public String generateContent(String prompt) throws Exception {
        return generateContent(prompt, Espera.PADRAO);
    }

    /** Igual a {@link #generateContent(String)}, com o tempo de espera do tipo de chamada. */
    public String generateContent(String prompt, Espera espera) throws Exception {
        if (apiKey == null || apiKey.isBlank()) {
            throw new IllegalStateException("GEMINI_API_KEY nao configurada. Defina a variavel de ambiente ou o fallback no application.properties.");
        }

        String url = String.format("%s/%s:generateContent", endpoint, model);

        Map<String, Object> body = Map.of(
            "contents", List.of(
                Map.of("parts", List.of(Map.of("text", prompt)))
            ),
            "generationConfig", Map.of(
                "temperature", temperature,
                "candidateCount", candidateCount,
                "maxOutputTokens", maxOutputTokens
            )
        );

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("x-goog-api-key", apiKey);

        HttpEntity<Map<String, Object>> request = new HttpEntity<>(body, headers);

        try {
            RestTemplate restTemplate = espera == Espera.CHECKIN ? restTemplateCheckin : restTemplatePadrao;
            ResponseEntity<Map> response = restTemplate.exchange(url, HttpMethod.POST, request, Map.class);

            if (response.getBody() == null) {
                throw new RuntimeException("Resposta vazia do Gemini API");
            }

            return extractText(response.getBody());

        } catch (Exception ex) {
            log.error("[GeminiClient] Erro ao chamar Gemini API: {}", ex.getMessage());
            throw ex;
        }
    }

    /**
     * Extrai o texto da resposta JSON do Gemini.
     */
    @SuppressWarnings("unchecked")
    private String extractText(Map<String, Object> responseBody) {
        try {
            List<Map<String, Object>> candidates = (List<Map<String, Object>>) responseBody.get("candidates");
            if (candidates == null || candidates.isEmpty()) {
                throw new RuntimeException("Nenhum candidato retornado pelo Gemini");
            }

            Map<String, Object> candidate = candidates.get(0);
            Map<String, Object> content = (Map<String, Object>) candidate.get("content");
            List<Map<String, Object>> parts = (List<Map<String, Object>>) content.get("parts");

            if (parts == null || parts.isEmpty()) {
                throw new RuntimeException("Nenhum texto retornado pelo Gemini");
            }

            return (String) parts.get(0).get("text");

        } catch (ClassCastException | NullPointerException ex) {
            log.error("[GeminiClient] Erro ao parsear resposta: {}", ex.getMessage());
            throw new RuntimeException("Formato de resposta inesperado do Gemini", ex);
        }
    }

    /**
     * Verifica se a chave de API esta configurada.
     */
    public boolean isConfigured() {
        return apiKey != null && !apiKey.isBlank();
    }
}
