package com.pranathi.pka.controller;

import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import com.pranathi.pka.dto.AgentRequest;
import com.pranathi.pka.dto.AgentResponse;
import com.pranathi.pka.service.AgentOrchestrationService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/agent")
public class AgentController {
    private final AgentOrchestrationService agent;

    public AgentController(AgentOrchestrationService agent) {
        this.agent = agent;
    }

    @PostMapping("/run")
    public AgentResponse run(@Valid @RequestBody AgentRequest request, Authentication authentication) {
        return agent.run(request, authentication.getName());
    }
}
