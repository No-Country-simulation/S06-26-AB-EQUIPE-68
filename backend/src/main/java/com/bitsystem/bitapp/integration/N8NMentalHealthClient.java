package com.bitsystem.bitapp.integration;

import com.bitsystem.bitapp.dto.MentalHealthDto;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class N8NMentalHealthClient extends N8NBaseClient {

    public N8NMentalHealthClient(
            @Value("${n8n.mental-health.url}") String webhookUrl,
            @Value("${n8n.connect-timeout}") int connectTimeout,
            @Value("${n8n.mental-health.timeout}") int readTimeout,
            @Value("${n8n.max-retries}") int maxRetries,
            @Value("${n8n.webhook-token:}") String webhookToken) {
        super("mental-health", webhookUrl, connectTimeout, readTimeout, maxRetries, webhookToken);
    }

    public MentalHealthDto.Response process(Object request) {
        return callWebhook(request, MentalHealthDto.Response.class);
    }
}
