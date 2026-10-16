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

    /** Cabeçalho que o nó "Webhook Entrada" do n8n confere (Header Auth). */
    public static final String HEADER_TOKEN = "X-Webhook-Token";

    // Só para medir o tamanho do payload no log: o conteúdo nunca é registrado.
    private static final ObjectMapper MEDIDOR = new ObjectMapper();

    protected final RestTemplate restTemplate;
    /** Rótulo do tipo de chamada ("mental-health", "assessment"), usado só no log. */
    protected final String tipo;
    protected final String webhookUrl;
    protected final int timeout;
    protected final int maxRetries;
    /** Valor de N8N_WEBHOOK_TOKEN; vazio = o webhook do n8n não exige cabeçalho. */
    private final String webhookToken;

    public N8NBaseClient(String tipo, String webhookUrl, int timeout, int maxRetries, String webhookToken) {
        this.tipo = tipo;
        this.webhookToken = webhookToken;
        this.webhookUrl = webhookUrl;
        this.timeout = timeout;
        this.maxRetries = maxRetries;

        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(Duration.ofSeconds(5));
        factory.setReadTimeout(Duration.ofMillis(timeout));
        this.restTemplate = new RestTemplate(factory);
    }

    /** false quando a URL do webhook está vazia: o chamador pula o n8n e usa o fallback local. */
    public boolean isConfigured() {
        return webhookUrl != null && !webhookUrl.isBlank();
    }

    protected <T, R> R callWebhook(T request, Class<R> responseType) {
        if (!isConfigured()) {
            throw new BusinessException("N8N_UNAVAILABLE", "Webhook do n8n não configurado.");
        }
        Exception lastException = null;

        for (int attempt = 1; attempt <= maxRetries; attempt++) {
            try {
                HttpHeaders headers = new HttpHeaders();
                headers.setContentType(MediaType.APPLICATION_JSON);
                if (webhookToken != null && !webhookToken.isBlank()) {
                    // Autenticação por cabeçalho do nó "Webhook Entrada"; o valor nunca vai para o log.
                    headers.set(HEADER_TOKEN, webhookToken);
                }

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
