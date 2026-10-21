package com.pranathi.pka.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

@Service
public class DocumentIndexingService {
    private static final int CHUNK_SIZE = 1200;
    private static final int CHUNK_OVERLAP = 200;

    private final JdbcTemplate jdbc;
    private final OpenAiClient ai;

    public DocumentIndexingService(
            @Qualifier("vectorJdbcTemplate") JdbcTemplate jdbc,
            OpenAiClient ai) {
        this.jdbc = jdbc;
        this.ai = ai;
    }

    @jakarta.annotation.PostConstruct
    public void initializeSchema() {
        jdbc.execute("""
                CREATE TABLE IF NOT EXISTS document_chunks (
                    id BIGSERIAL PRIMARY KEY,
                    user_email VARCHAR(320) NOT NULL,
                    document_id BIGINT NOT NULL,
                    filename TEXT NOT NULL,
                    chunk_index INTEGER NOT NULL,
                    chunk_text TEXT NOT NULL,
                    embedding vector(768) NOT NULL,
                    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
                    UNIQUE (user_email, document_id, chunk_index)
                )
                """);
        jdbc.execute("CREATE INDEX IF NOT EXISTS idx_document_chunks_owner_document ON document_chunks(user_email, document_id)");
    }

    public int index(Long documentId, String email, String filename, String text) {
        delete(documentId, email);
        List<String> chunks = split(text);
        for (int i = 0; i < chunks.size(); i++) {
            String chunk = chunks.get(i);
            float[] embedding = ai.embed(chunk);
            jdbc.update("""
                    INSERT INTO document_chunks
                        (user_email, document_id, filename, chunk_index, chunk_text, embedding)
                    VALUES (?, ?, ?, ?, ?, CAST(? AS vector))
                    ON CONFLICT (user_email, document_id, chunk_index)
                    DO UPDATE SET filename = EXCLUDED.filename,
                                  chunk_text = EXCLUDED.chunk_text,
                                  embedding = EXCLUDED.embedding,
                                  created_at = NOW()
                    """, email, documentId, filename, i, chunk, vectorLiteral(embedding));
        }
        return chunks.size();
    }

    public void delete(Long documentId, String email) {
        jdbc.update("DELETE FROM document_chunks WHERE document_id = ? AND user_email = ?", documentId, email);
    }

    public List<Map<String, Object>> search(String email, float[] queryEmbedding, int limit) {
        String vector = vectorLiteral(queryEmbedding);
        return jdbc.queryForList("""
                SELECT document_id, filename, chunk_index, chunk_text,
                       1 - (embedding <=> CAST(? AS vector)) AS similarity
                FROM document_chunks
                WHERE user_email = ?
                ORDER BY embedding <=> CAST(? AS vector)
                LIMIT ?
                """, vector, email, vector, limit);
    }

    private List<String> split(String text) {
        List<String> chunks = new ArrayList<>();
        if (text == null || text.isBlank()) return chunks;
        int start = 0;
        while (start < text.length()) {
            int end = Math.min(start + CHUNK_SIZE, text.length());
            chunks.add(text.substring(start, end));
            if (end == text.length()) break;
            start = end - CHUNK_OVERLAP;
        }
        return chunks;
    }

    private String vectorLiteral(float[] vector) {
        StringBuilder value = new StringBuilder("[");
        for (int i = 0; i < vector.length; i++) {
            if (i > 0) value.append(',');
            value.append(vector[i]);
        }
        return value.append(']').toString();
    }
}
