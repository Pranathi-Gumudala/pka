package com.pranathi.pka.service;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

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

    public RagService(NoteIndexingService indexingService, OpenAiClient openAi, NoteRepository noteRepository) {
        this.indexingService = indexingService;
        this.openAi = openAi;
        this.noteRepository = noteRepository;
    }

    public RagResponse ask(String question, String email) {
        return ask(question, email, List.of());
    }

    public RagResponse ask(String question, String email, List<Map<String, String>> history) {
        float[] embedding = openAi.embed(question);
        List<Map<String, Object>> matches = indexingService.search(email, embedding, 5);
        if (matches.isEmpty()) {
            return new RagResponse("I couldn't find any indexed notes for your account. Click “Reindex my notes” and try again.", List.of());
        }

        StringBuilder context = new StringBuilder();
        Map<Long, RagSource> sources = new LinkedHashMap<>();
        for (Map<String, Object> match : matches) {
            Long noteId = ((Number) match.get("note_id")).longValue();
            String title = String.valueOf(match.get("note_title"));
            String chunk = String.valueOf(match.get("chunk_text"));
            double similarity = ((Number) match.get("similarity")).doubleValue();
            context.append("\n--- Note: ").append(title).append(" (id ").append(noteId)
                    .append(") ---\n").append(chunk).append("\n");
            sources.putIfAbsent(noteId, new RagSource(noteId, title, similarity));
        }
        String answer = openAi.answer(question, context.toString(), history);
        return new RagResponse(answer, new ArrayList<>(sources.values()));
    }

    public int reindex(String email) {
        List<Note> notes = noteRepository.findByUser_Email(email);
        return indexingService.reindexNotes(notes, email);
    }
}
