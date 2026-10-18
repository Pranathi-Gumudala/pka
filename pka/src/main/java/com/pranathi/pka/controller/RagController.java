package com.pranathi.pka.controller;

import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import com.pranathi.pka.dto.RagRequest;
import com.pranathi.pka.dto.RagResponse;
import com.pranathi.pka.service.RagService;

import jakarta.validation.Valid;
import java.util.Map;

@RestController
@RequestMapping("/api/rag")
public class RagController {

    private final RagService ragService;

    public RagController(RagService ragService) {
        this.ragService = ragService;
    }

    @PostMapping("/ask")
    public RagResponse ask(@Valid @RequestBody RagRequest request, Authentication authentication) {
        return ragService.ask(request.question(), authentication.getName());
    }

    @PostMapping("/reindex")
    public Map<String, Object> reindex(Authentication authentication) {
        int count = ragService.reindex(authentication.getName());
        return Map.of("indexedNotes", count, "message", "Reindex complete");
    }
}
