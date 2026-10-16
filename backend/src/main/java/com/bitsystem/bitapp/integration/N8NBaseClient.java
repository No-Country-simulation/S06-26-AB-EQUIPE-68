package com.bitsystem.bitapp.integration;

import com.bitsystem.bitapp.exception.BusinessException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.Duration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;

public abstract class N8NBaseClient {

    private static final Logger log = LoggerFactory.getLogger(N8NBaseClient.class);

    // Só para medir o tamanho do payload no log: o conteúdo nunca é registrado.
    private static final ObjectMapper MEDIDOR = new ObjectMapper();

    protected final RestTemplate restTemplate;
    /** Rótulo do tipo de chamada ("mental-health", "assessment"), usado só no log. */
    protected final String tipo;
    protected final String webhookUrl;
    protected final int timeout;
    protected final int maxRetries;

    public N8NBaseClient(String tipo, String webhookUrl, int timeout, int maxRetries) {
        this.tipo = tipo;
        this.webhookUrl = webhookUrl;
        this.timeout = timeout;
        this.maxRetries = maxRetries;

        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(Duration.ofSeconds(5));
        factory.setReadTimeout(Duration.ofMillis(timeout));
        this.restTemplate = new RestTemplate(factory);
    }

    protected <T, R> R callWebhook(T request, Class<R> responseType) {
        Exception lastException = null;

        for (int attempt = 1; attempt <= maxRetries; attempt++) {
            try {
                HttpHeaders headers = new HttpHeaders();
                headers.setContentType(MediaType.APPLICATION_JSON);

                HttpEntity<T> entity = new HttpEntity<>(request, headers);

                // Só metadados: o payload carrega o texto livre do check-in e não vai para o log.
                log.info("Chamando webhook n8n [tipo={}] tentativa {}/{} (payload de {} bytes)",
                        tipo, attempt, maxRetries, tamanhoEmBytes(request));
                ResponseEntity<R> response = restTemplate.postForEntity(
                        webhookUrl, entity, responseType);
                log.debug("Resposta do n8n [tipo={}]: status {}", tipo, response.getStatusCode());
                return response.getBody();

            } catch (Exception ex) {
                lastException = ex;
                log.warn("N8N webhook call failed (attempt {}/{}): {}", attempt, maxRetries, ex.getMessage());
                if (attempt < maxRetries) {
                    try {
                        Thread.sleep(Duration.ofSeconds(1).toMillis());
                    } catch (InterruptedException ie) {
                        Thread.currentThread().interrupt();
                        break;
                    }
                }
            }
        }

        throw new BusinessException("N8N_UNAVAILABLE",
                "Serviço de orquestração temporariamente indisponível. Tente novamente mais tarde.");
    }

    private static int tamanhoEmBytes(Object payload) {
        try {
            return MEDIDOR.writeValueAsBytes(payload).length;
        } catch (Exception ex) {
            return -1;
        }
    }
}
