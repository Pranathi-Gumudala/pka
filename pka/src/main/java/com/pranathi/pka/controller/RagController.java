package com.pranathi.pka.controller;

import java.util.List;
import java.util.Map;

import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import com.pranathi.pka.dto.*;
import com.pranathi.pka.service.RagConversationService;
import com.pranathi.pka.service.RagService;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/rag")
public class RagController {
    private final RagService ragService;
    private final RagConversationService conversationService;

    public RagController(RagService ragService, RagConversationService conversationService) {
        this.ragService = ragService;
        this.conversationService = conversationService;
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

    @GetMapping("/conversations")
    public List<RagConversationDto> listConversations(Authentication authentication) {
        return conversationService.list(authentication.getName());
    }

    @PostMapping("/conversations")
    public RagConversationDto createConversation(@RequestBody(required = false) RagConversationRequest request,
                                                 Authentication authentication) {
        return conversationService.create(authentication.getName(), request == null ? null : request.title());
    }

    @GetMapping("/conversations/{id}/messages")
    public List<RagChatMessageDto> getMessages(@PathVariable Long id,
            @RequestParam(required = false) Long leafMessageId, Authentication authentication) {
        return conversationService.getMessages(id, leafMessageId, authentication.getName());
    }

    @PostMapping("/conversations/{id}/messages")
    public List<RagChatMessageDto> askInConversation(@PathVariable Long id,
            @Valid @RequestBody RagChatMessageRequest request, Authentication authentication) {
        return conversationService.ask(id, request, authentication.getName());
    }
}
