package com.pranathi.pka.service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.pranathi.pka.dto.*;
import com.pranathi.pka.entity.RagConversation;
import com.pranathi.pka.entity.RagMessage;
import com.pranathi.pka.repository.RagConversationRepository;
import com.pranathi.pka.repository.RagMessageRepository;

@Service
public class RagConversationService {
    private final RagConversationRepository conversations;
    private final RagMessageRepository messages;
    private final RagService ragService;
    private final AgentOrchestrationService agentService;
    private final ObjectMapper mapper;

    public RagConversationService(RagConversationRepository conversations, RagMessageRepository messages,
                                  RagService ragService, AgentOrchestrationService agentService,
                                  ObjectMapper mapper) {
        this.conversations = conversations;
        this.messages = messages;
        this.ragService = ragService;
        this.agentService = agentService;
        this.mapper = mapper;
    }

    public List<RagConversationDto> list(String email) {
        return conversations.findByOwnerEmailOrderByUpdatedAtDesc(email).stream()
                .map(this::toConversationDto).toList();
    }

    @Transactional
    public RagConversationDto create(String email, String title) {
        return toConversationDto(conversations.save(new RagConversation(email, title)));
    }

    @Transactional(readOnly = true)
    public List<RagChatMessageDto> getMessages(Long conversationId, Long leafMessageId, String email) {
        RagConversation conversation = ownedConversation(conversationId, email);
        List<RagMessage> all = messages.findByConversation_IdOrderByCreatedAtAsc(conversation.getId());
        if (all.isEmpty()) return List.of();
        Long leaf = leafMessageId;
        if (leaf == null) {
            for (RagMessage message : all) {
                if ("assistant".equals(message.getRole())) leaf = message.getId();
            }
            if (leaf == null) leaf = all.get(all.size() - 1).getId();
        }
        return lineage(all, leaf).stream().map(this::toMessageDto).toList();
    }

    @Transactional
    public List<RagChatMessageDto> ask(Long conversationId, RagChatMessageRequest request, String email) {
        RagConversation conversation = ownedConversation(conversationId, email);
        List<RagMessage> all = messages.findByConversation_IdOrderByCreatedAtAsc(conversationId);
        List<RagMessage> ancestors = request.parentMessageId() == null
                ? List.of() : lineage(all, request.parentMessageId());
        if (request.parentMessageId() != null && ancestors.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Parent message is not in this conversation.");
        }
        if (!ancestors.isEmpty() && !"assistant".equals(ancestors.get(ancestors.size() - 1).getRole())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "A branch must start from an assistant response.");
        }

        List<Map<String, String>> history = new ArrayList<>();
        for (RagMessage message : ancestors) {
            history.add(Map.of("role", message.getRole(), "content", message.getContent()));
        }
        RagMessage userMessage = messages.save(new RagMessage(conversation,
                request.parentMessageId(), "user", request.question().trim(), "[]"));

        String task = request.task() == null ? "ask" : request.task();
        RagResponse response;
        if ("ask".equals(task)) {
            response = ragService.ask(request.question().trim(), email, history);
        } else {
            AgentResponse agentResponse = agentService.run(
                    new AgentRequest(task, request.question().trim()), email);
            response = new RagResponse(agentResponse.answer(), agentResponse.sources());
        }
        String sourcesJson;
        try {
            sourcesJson = mapper.writeValueAsString(response.sources());
        } catch (Exception e) {
            sourcesJson = "[]";
        }
        RagMessage assistantMessage = messages.save(new RagMessage(conversation,
                userMessage.getId(), "assistant", response.answer(), sourcesJson));
        if (all.isEmpty() && (conversation.getTitle() == null || conversation.getTitle().equals("New conversation"))) {
            String title = request.question().trim();
            conversation.setTitle(title.substring(0, Math.min(80, title.length())));
        }
        conversation.touch();
        conversations.save(conversation);
        return List.of(toMessageDto(userMessage), toMessageDto(assistantMessage));
    }

    private RagConversation ownedConversation(Long id, String email) {
        return conversations.findByIdAndOwnerEmail(id, email)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Conversation not found."));
    }

    private List<RagMessage> lineage(List<RagMessage> all, Long leafId) {
        java.util.Map<Long, RagMessage> byId = new java.util.HashMap<>();
        for (RagMessage message : all) byId.put(message.getId(), message);
        List<RagMessage> path = new ArrayList<>();
        java.util.Set<Long> visited = new java.util.HashSet<>();
        Long current = leafId;
        while (current != null && visited.add(current)) {
            RagMessage message = byId.get(current);
            if (message == null) return List.of();
            path.add(message);
            current = message.getParentMessageId();
        }
        Collections.reverse(path);
        return path;
    }

    private RagConversationDto toConversationDto(RagConversation c) {
        return new RagConversationDto(c.getId(), c.getTitle(), c.getCreatedAt(), c.getUpdatedAt());
    }

    private RagChatMessageDto toMessageDto(RagMessage m) {
        List<RagSource> sources;
        try {
            sources = mapper.readValue(m.getSourcesJson(), new TypeReference<List<RagSource>>() {});
        } catch (Exception e) {
            sources = List.of();
        }
        return new RagChatMessageDto(m.getId(), m.getParentMessageId(), m.getRole(), m.getContent(), sources, m.getCreatedAt());
    }
}
