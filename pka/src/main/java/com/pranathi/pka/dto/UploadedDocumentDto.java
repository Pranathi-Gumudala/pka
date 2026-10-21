package com.pranathi.pka.dto;

import java.time.Instant;

import com.pranathi.pka.entity.DocumentStatus;
import com.pranathi.pka.entity.UploadedDocument;

public record UploadedDocumentDto(
        Long id,
        String filename,
        String mediaType,
        DocumentStatus status,
        String errorMessage,
        Instant createdAt,
        Instant updatedAt) {
    public static UploadedDocumentDto from(UploadedDocument document) {
        return new UploadedDocumentDto(
                document.getId(),
                document.getOriginalFilename(),
                document.getMediaType(),
                document.getStatus(),
                document.getErrorMessage(),
                document.getCreatedAt(),
                document.getUpdatedAt());
    }
}
