package com.pranathi.pka.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import com.pranathi.pka.entity.Note;

@Service
public class NoteIndexingService {

    private static final int CHUNK_SIZE = 1200;
    private static final int CHUNK_OVERLAP = 200;

    private final JdbcTemplate jdbc;
    private final OpenAiClient openAi;

    public NoteIndexingService(
            @Qualifier("vectorJdbcTemplate") JdbcTemplate jdbc,
            OpenAiClient openAi) {
        this.jdbc = jdbc;
        this.openAi = openAi;
    }

    @jakarta.annotation.PostConstruct
    public void initializeSchema() {
        jdbc.execute("CREATE EXTENSION IF NOT EXISTS vector");
        jdbc.execute("""
                CREATE TABLE IF NOT EXISTS note_chunks (
                    id BIGSERIAL PRIMARY KEY,
                    user_email VARCHAR(320) NOT NULL,
                    note_id BIGINT NOT NULL,
                    note_title TEXT NOT NULL,
                    chunk_index INTEGER NOT NULL,
                    chunk_text TEXT NOT NULL,
                    embedding vector(1536) NOT NULL,
                    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
                    UNIQUE (user_email, note_id, chunk_index)
                )
                """);
        jdbc.execute("CREATE INDEX IF NOT EXISTS idx_note_chunks_user_note ON note_chunks(user_email, note_id)");
    }

    public void indexNote(Note note, String email) {
        deleteNote(note.getId(), email);
        String text = safe(note.getTitle()) + "\n\n" + safe(note.getContent());
        List<String> chunks = splitIntoChunks(text);
        for (int i = 0; i < chunks.size(); i++) {
            String chunk = chunks.get(i);
            float[] embedding = openAi.embed(chunk);
            jdbc.update("""
                    INSERT INTO note_chunks
                        (user_email, note_id, note_title, chunk_index, chunk_text, embedding)
                    VALUES (?, ?, ?, ?, ?, CAST(? AS vector))
                    ON CONFLICT (user_email, note_id, chunk_index)
                    DO UPDATE SET note_title = EXCLUDED.note_title,
                                  chunk_text = EXCLUDED.chunk_text,
                                  embedding = EXCLUDED.embedding,
                                  created_at = NOW()
                    """,
                    email, note.getId(), note.getTitle(), i, chunk, toVectorLiteral(embedding));
        }
    }

    public void deleteNote(Long noteId, String email) {
        jdbc.update("DELETE FROM note_chunks WHERE note_id = ? AND user_email = ?", noteId, email);
    }

    public int reindexNotes(List<Note> notes, String email) {
        jdbc.update("DELETE FROM note_chunks WHERE user_email = ?", email);
        int count = 0;
        for (Note note : notes) {
            indexNote(note, email);
            count++;
        }
        return count;
    }

    public List<Map<String, Object>> search(String email, float[] queryEmbedding, int limit) {
        return jdbc.queryForList("""
                SELECT note_id, note_title, chunk_text,
                       1 - (embedding <=> CAST(? AS vector)) AS similarity
                FROM note_chunks
                WHERE user_email = ?
                ORDER BY embedding <=> CAST(? AS vector)
                LIMIT ?
                """,
                toVectorLiteral(queryEmbedding), email, toVectorLiteral(queryEmbedding), limit);
    }

    private List<String> splitIntoChunks(String text) {
        List<String> chunks = new ArrayList<>();
        if (text.isBlank()) return chunks;
        int start = 0;
        while (start < text.length()) {
            int end = Math.min(start + CHUNK_SIZE, text.length());
            chunks.add(text.substring(start, end));
            if (end == text.length()) break;
            start = end - CHUNK_OVERLAP;
        }
        return chunks;
    }

    private String toVectorLiteral(float[] vector) {
        StringBuilder value = new StringBuilder("[");
        for (int i = 0; i < vector.length; i++) {
            if (i > 0) value.append(',');
            value.append(vector[i]);
        }
        return value.append(']').toString();
    }

    private String safe(String value) {
        return value == null ? "" : value;
    }
}
