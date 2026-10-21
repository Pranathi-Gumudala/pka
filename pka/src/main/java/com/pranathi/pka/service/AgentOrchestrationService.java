package com.pranathi.pka.service;

import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import com.pranathi.pka.dto.AgentRequest;
import com.pranathi.pka.dto.AgentResponse;

@Service
public class AgentOrchestrationService {
    private final RestClient client;
    private final String internalToken;

    public AgentOrchestrationService(
            @Value("${app.agent.base-url:http://127.0.0.1:8000}") String baseUrl,
            @Value("${INTERNAL_SERVICE_TOKEN:}") String internalToken) {
        this.client = RestClient.builder().baseUrl(baseUrl).build();
        this.internalToken = internalToken;
    }

    public AgentResponse run(AgentRequest request, String ownerEmail) {
        if (internalToken == null || internalToken.isBlank()) {
            throw new IllegalStateException("INTERNAL_SERVICE_TOKEN is not configured.");
        }
        return client.post().uri("/agent/run")
                .header("X-Internal-Token", internalToken)
                .body(Map.of(
                        "task", request.task().trim().toLowerCase(),
                        "question", request.question().trim(),
                        "ownerEmail", ownerEmail))
                .retrieve()
                .body(AgentResponse.class);
    }
}
