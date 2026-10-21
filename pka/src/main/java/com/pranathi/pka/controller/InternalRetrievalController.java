package com.pranathi.pka.controller;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.http.HttpStatus;

import com.pranathi.pka.dto.InternalRetrieveRequest;
import com.pranathi.pka.dto.InternalRetrieveResponse;
import com.pranathi.pka.dto.RagSource;
import com.pranathi.pka.service.NoteIndexingService;
import com.pranathi.pka.service.OpenAiClient;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/internal/rag")
public class InternalRetrievalController {
    private final NoteIndexingService indexing;
    private final OpenAiClient ai;
    private final String serviceToken;
    private final int topK;

    public InternalRetrievalController(
            NoteIndexingService indexing,
            OpenAiClient ai,
            @Value("${INTERNAL_SERVICE_TOKEN:}") String serviceToken,
            @Value("${app.rag.retrieval-top-k:5}") int topK) {
        this.indexing = indexing;
        this.ai = ai;
        this.serviceToken = serviceToken;
        this.topK = Math.max(1, Math.min(topK, 12));
    }

    @PostMapping("/retrieve")
    public InternalRetrieveResponse retrieve(
            @RequestHeader(value = "X-Internal-Token", required = false) String suppliedToken,
            @Valid @RequestBody InternalRetrieveRequest request) {
        if (serviceToken.isBlank() || suppliedToken == null
                || !MessageDigest.isEqual(serviceToken.getBytes(StandardCharsets.UTF_8),
                                          suppliedToken.getBytes(StandardCharsets.UTF_8))) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Internal service authentication failed.");
        }

        float[] embedding = ai.embed(request.query());
        List<Map<String, Object>> matches = indexing.search(request.ownerEmail(), embedding, topK);
        StringBuilder context = new StringBuilder();
        Map<String, RagSource> sources = new LinkedHashMap<>();
        for (Map<String, Object> match : matches) {
            String type = String.valueOf(match.get("source_type"));
            String chunk = String.valueOf(match.get("chunk_text"));
            double similarity = ((Number) match.get("similarity")).doubleValue();
            if ("document".equals(type)) {
                Long id = ((Number) match.get("document_id")).longValue();
                String filename = String.valueOf(match.get("filename"));
                Integer chunkIndex = ((Number) match.get("chunk_index")).intValue();
                context.append("\n--- Uploaded document: ").append(filename)
                        .append(" (document id ").append(id).append(", chunk ")
                        .append(chunkIndex + 1).append(") ---\n").append(chunk).append("\n");
                sources.putIfAbsent("document:" + id,
                        RagSource.document(id, filename, chunkIndex, similarity));
            } else {
                Long id = ((Number) match.get("note_id")).longValue();
                String title = String.valueOf(match.get("note_title"));
                context.append("\n--- Note: ").append(title).append(" (id ")
                        .append(id).append(") ---\n").append(chunk).append("\n");
                sources.putIfAbsent("note:" + id, new RagSource(id, title, similarity));
            }
        }
        return new InternalRetrieveResponse(context.toString(), new ArrayList<>(sources.values()));
    }
}
