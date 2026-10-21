package com.pranathi.pka.service;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.pranathi.pka.dto.RagResponse;
import com.pranathi.pka.dto.RagSource;
import com.pranathi.pka.entity.Note;
import com.pranathi.pka.repository.NoteRepository;

@Service
public class RagService {
    private final NoteIndexingService indexingService;
    private final OpenAiClient openAi;
    private final NoteRepository noteRepository;
    private final int retrievalTopK;

    public RagService(NoteIndexingService indexingService, OpenAiClient openAi,
                      NoteRepository noteRepository,
                      @Value("${app.rag.retrieval-top-k:5}") int retrievalTopK) {
        this.indexingService = indexingService;
        this.openAi = openAi;
        this.noteRepository = noteRepository;
        this.retrievalTopK = Math.max(1, Math.min(retrievalTopK, 12));
    }

    public RagResponse ask(String question, String email) {
        return ask(question, email, List.of());
    }

    public RagResponse ask(String question, String email, List<Map<String, String>> history) {
        float[] embedding = openAi.embed(question);
        List<Map<String, Object>> matches = indexingService.search(email, embedding, retrievalTopK);
        if (matches.isEmpty()) {
            return new RagResponse("I couldn't find any indexed notes or documents for your account. "
                    + "Reindex your notes or upload a document, then try again.", List.of());
        }

        StringBuilder context = new StringBuilder();
        Map<String, RagSource> sources = new LinkedHashMap<>();
        for (Map<String, Object> match : matches) {
            String sourceType = String.valueOf(match.get("source_type"));
            String chunk = String.valueOf(match.get("chunk_text"));
            double similarity = ((Number) match.get("similarity")).doubleValue();

            if ("document".equals(sourceType)) {
                Long documentId = ((Number) match.get("document_id")).longValue();
                String filename = String.valueOf(match.get("filename"));
                Integer chunkIndex = ((Number) match.get("chunk_index")).intValue();
                context.append("\n--- Uploaded document: ").append(filename)
                        .append(" (document id ").append(documentId)
                        .append(", chunk ").append(chunkIndex + 1).append(") ---\n")
                        .append(chunk).append("\n");
                sources.putIfAbsent("document:" + documentId,
                        RagSource.document(documentId, filename, chunkIndex, similarity));
            } else {
                Long noteId = ((Number) match.get("note_id")).longValue();
                String title = String.valueOf(match.get("note_title"));
                context.append("\n--- Note: ").append(title).append(" (id ").append(noteId)
                        .append(") ---\n").append(chunk).append("\n");
                sources.putIfAbsent("note:" + noteId, new RagSource(noteId, title, similarity));
            }
        }
        String answer = openAi.answer(question, context.toString(), history);
        return new RagResponse(answer, new ArrayList<>(sources.values()));
    }

    public int reindex(String email) {
        List<Note> notes = noteRepository.findByUser_Email(email);
        return indexingService.reindexNotes(notes, email);
    }
}
