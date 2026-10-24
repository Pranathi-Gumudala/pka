package com.pranathi.pka.dto;

public record RagSource(
        Long noteId,
        String title,
        double similarity,
        String sourceType,
        Long documentId,
        String filename,
        Integer chunkIndex) {

    public RagSource(Long noteId, String title, double similarity) {
        this(noteId, title, similarity, "note", null, null, null);
    }

    public static RagSource document(Long documentId, String filename, Integer chunkIndex, double similarity) {
        return new RagSource(null, filename, similarity, "document", documentId, filename, chunkIndex);
    }
}
