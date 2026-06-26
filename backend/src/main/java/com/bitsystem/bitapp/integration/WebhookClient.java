package com.bitsystem.bitapp.integration;

import com.bitsystem.bitapp.exception.BusinessException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

public class WebhookClient {

    private static final Logger log = LoggerFactory.getLogger(WebhookClient.class);

    private final RestTemplate restTemplate;

    public WebhookClient() {
        this.restTemplate = new RestTemplate();
    }

    public <T, R> R call(String url, T request, Class<R> responseType) {
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            HttpEntity<T> entity = new HttpEntity<>(request, headers);

            log.info("Calling webhook: {}", url);
            ResponseEntity<R> response = restTemplate.postForEntity(url, entity, responseType);
            return response.getBody();

        } catch (Exception ex) {
            log.error("Webhook call failed: {}", ex.getMessage());
            throw new BusinessException("WEBHOOK_ERROR", "Erro na comunicação com webhook: " + ex.getMessage());
        }
    }
}
