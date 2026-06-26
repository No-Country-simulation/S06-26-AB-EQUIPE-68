package com.bitsystem.bitapp.client;

import com.bitsystem.bitapp.config.GoogleGeminiProperties;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

/**
 * Cliente HTTP para chamadas a API Google Gemini (v1beta).
 *
 * Autenticacao via header x-goog-api-key, compativel com chaves
 * AIza (antigas) e AQ. (novas auth keys do Google AI Studio).
 *
 * @author BiT System
 * @version 1.1.0
 */
@Component
public class GoogleGeminiClient {

    private final GoogleGeminiProperties properties;
    private final RestTemplate restTemplate;

    public GoogleGeminiClient(GoogleGeminiProperties properties) {
        this.properties = properties;
        this.restTemplate = new RestTemplate();
    }

    public String generateText(String prompt) {
        // Monta URL completa do endpoint Gemini
        String url = properties.getEndpoint() + "/" + properties.getModel() + ":generateContent";

        // Constroi URI sem a chave no query param (chaves AQ. usam header)
        UriComponentsBuilder uriBuilder = UriComponentsBuilder.fromHttpUrl(url);

        // Prepara headers HTTP
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        // Autenticacao via header (compativel com AIza e AQ.)
        headers.set("x-goog-api-key", properties.getApiKey());

        // Constroi payload de requisicao Gemini (formato v1beta)
        Map<String, Object> requestBody = Map.of(
                "contents",
                List.of(Map.of("role", "user", "parts", List.of(Map.of("text", prompt)))),
                "generationConfig",
                Map.of(
                        "temperature", properties.getTemperature(),
                        "candidateCount", properties.getCandidateCount()
                )
        );

        // Encapsula request em HttpEntity
        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);

        // Executa POST HTTP
        ResponseEntity<Map> response = restTemplate.exchange(
                uriBuilder.build().toUri(),
                HttpMethod.POST,
                entity,
                Map.class
        );

        // Valida resposta
        if (response.getBody() == null || response.getBody().get("candidates") == null) {
            throw new IllegalStateException("Resposta vazia do Gemini");
        }

        // Extrai texto do primeiro candidate
        @SuppressWarnings("unchecked")
        List<Map<String, Object>> candidates = (List<Map<String, Object>>) response.getBody().get("candidates");
        if (candidates.isEmpty()) {
            throw new IllegalStateException("Nenhum candidate retornado pelo Gemini");
        }

        Map<String, Object> firstCandidate = candidates.get(0);
        @SuppressWarnings("unchecked")
        Map<String, Object> content = (Map<String, Object>) firstCandidate.get("content");
        @SuppressWarnings("unchecked")
        List<Map<String, String>> parts = (List<Map<String, String>>) content.get("parts");

        return parts.stream()
                .map(part -> part.get("text"))
                .filter(Objects::nonNull)
                .reduce("", String::concat);
    }
}