package com.bitsystem.bitapp.integration;

import com.bitsystem.bitapp.dto.AssessmentDto;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class N8NAssessmentClient extends N8NBaseClient {

    public N8NAssessmentClient(
            @Value("${n8n.assessment.url}") String webhookUrl,
            @Value("${n8n.connect-timeout}") int connectTimeout,
            @Value("${n8n.assessment.timeout}") int readTimeout,
            @Value("${n8n.max-retries}") int maxRetries,
            @Value("${n8n.webhook-token:}") String webhookToken) {
        super("assessment", webhookUrl, connectTimeout, readTimeout, maxRetries, webhookToken);
    }

    public AssessmentDto.Response process(AssessmentDto.Request request) {
        return callWebhook(request, AssessmentDto.Response.class);
    }
}
