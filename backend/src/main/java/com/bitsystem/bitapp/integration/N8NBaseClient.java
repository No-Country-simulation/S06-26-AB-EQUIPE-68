package com.bitsystem.bitapp.integration;

import com.bitsystem.bitapp.exception.BusinessException;
import java.time.Duration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

public abstract class N8NBaseClient {

    private static final Logger log = LoggerFactory.getLogger(N8NBaseClient.class);

    protected final RestTemplate restTemplate;
    protected final String webhookUrl;
    protected final int timeout;
    protected final int maxRetries;

    public N8NBaseClient(String webhookUrl, int timeout, int maxRetries) {
        this.webhookUrl = webhookUrl;
        this.timeout = timeout;
        this.maxRetries = maxRetries;
        this.restTemplate = new RestTemplate();
    }

    protected <T, R> R callWebhook(T request, Class<R> responseType) {
        Exception lastException = null;

        for (int attempt = 1; attempt <= maxRetries; attempt++) {
            try {
                HttpHeaders headers = new HttpHeaders();
                headers.setContentType(MediaType.APPLICATION_JSON);

                HttpEntity<T> entity = new HttpEntity<>(request, headers);

                log.info("Calling N8N webhook [{}] attempt {}/{} with payload: {}", webhookUrl, attempt, maxRetries, request);
                ResponseEntity<R> response = restTemplate.postForEntity(
                        webhookUrl, entity, responseType);
                log.debug("N8N response status: {}, body: {}", response.getStatusCode(), response.getBody());
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
}
