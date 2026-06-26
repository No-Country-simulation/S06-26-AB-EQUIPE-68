package com.bitsystem.bitapp.integration;

import com.bitsystem.bitapp.dto.MentalHealthDto;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class N8NMentalHealthClient extends N8NBaseClient {

    public N8NMentalHealthClient(
            @Value("${n8n.mental-health.url}") String webhookUrl,
            @Value("${n8n.timeout}") int timeout,
            @Value("${n8n.max-retries}") int maxRetries) {
        super(webhookUrl, timeout, maxRetries);
    }

    public MentalHealthDto.Response process(Object request) {
        return callWebhook(request, MentalHealthDto.Response.class);
    }
}
