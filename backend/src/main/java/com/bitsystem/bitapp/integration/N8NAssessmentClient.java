package com.bitsystem.bitapp.integration;

import com.bitsystem.bitapp.dto.AssessmentDto;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class N8NAssessmentClient extends N8NBaseClient {

    public N8NAssessmentClient(
            @Value("${n8n.assessment.url}") String webhookUrl,
            @Value("${n8n.timeout}") int timeout,
            @Value("${n8n.max-retries}") int maxRetries) {
        super(webhookUrl, timeout, maxRetries);
    }

    public AssessmentDto.Response process(AssessmentDto.Request request) {
        return callWebhook(request, AssessmentDto.Response.class);
    }
}
