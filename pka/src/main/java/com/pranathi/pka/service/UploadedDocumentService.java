package com.pranathi.pka.service;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import org.apache.tika.Tika;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.transaction.annotation.Transactional;

import com.pranathi.pka.dto.UploadedDocumentDto;
import com.pranathi.pka.entity.DocumentStatus;
import com.pranathi.pka.entity.UploadedDocument;
import com.pranathi.pka.repository.UploadedDocumentRepository;

@Service
public class UploadedDocumentService {
    private static final Set<String> ALLOWED_EXTENSIONS = Set.of("pdf", "txt", "docx", "pptx");
    private final UploadedDocumentRepository documents;
    private final DocumentIndexingService indexing;
    private final Tika tika = new Tika();

    public UploadedDocumentService(UploadedDocumentRepository documents, DocumentIndexingService indexing) {
        this.documents = documents;
        this.indexing = indexing;
    }

    @Transactional
    public UploadedDocumentDto upload(MultipartFile file, String email) {
        if (file == null || file.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Choose a non-empty document.");
        }
        String filename = safeFilename(file.getOriginalFilename());
        String extension = extension(filename);
        if (!ALLOWED_EXTENSIONS.contains(extension)) {
            throw new ResponseStatusException(HttpStatus.UNSUPPORTED_MEDIA_TYPE,
                    "Supported formats are PDF, TXT, DOCX and PPTX.");
        }

        UploadedDocument document = new UploadedDocument();
        document.setOwnerEmail(email);
        document.setOriginalFilename(filename);
        document.setMediaType(file.getContentType() == null ? "application/octet-stream" : file.getContentType());
        document.setStatus(DocumentStatus.PROCESSING);
        document = documents.save(document);

        try {
            String extracted = tika.parseToString(new ByteArrayInputStream(file.getBytes()));
            if (extracted == null || extracted.isBlank()) {
                throw new IllegalArgumentException("No extractable text was found. Scanned PDFs may need OCR.");
            }
            document.setExtractedText(extracted);
            indexing.index(document.getId(), email, filename, extracted);
            document.setStatus(DocumentStatus.READY);
            document.setErrorMessage(null);
        } catch (IOException | RuntimeException ex) {
            indexing.delete(document.getId(), email);
            document.setStatus(DocumentStatus.FAILED);
            document.setErrorMessage(safeError(ex));
        }
        return UploadedDocumentDto.from(documents.save(document));
    }

    @Transactional(readOnly = true)
    public List<UploadedDocumentDto> list(String email) {
        return documents.findByOwnerEmailOrderByCreatedAtDesc(email).stream()
                .map(UploadedDocumentDto::from).toList();
    }

    @Transactional(readOnly = true)
    public UploadedDocumentDto get(Long id, String email) {
        return UploadedDocumentDto.from(owned(id, email));
    }

    @Transactional
    public UploadedDocumentDto reindex(Long id, String email) {
        UploadedDocument document = owned(id, email);
        if (document.getExtractedText() == null || document.getExtractedText().isBlank()) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "This document has no extracted text to reindex. Upload it again.");
        }
        document.setStatus(DocumentStatus.PROCESSING);
        document.setErrorMessage(null);
        documents.save(document);
        try {
            indexing.index(id, email, document.getOriginalFilename(), document.getExtractedText());
            document.setStatus(DocumentStatus.READY);
        } catch (RuntimeException ex) {
            indexing.delete(id, email);
            document.setStatus(DocumentStatus.FAILED);
            document.setErrorMessage(safeError(ex));
        }
        return UploadedDocumentDto.from(documents.save(document));
    }

    @Transactional
    public void delete(Long id, String email) {
        UploadedDocument document = owned(id, email);
        indexing.delete(id, email);
        documents.delete(document);
    }

    private UploadedDocument owned(Long id, String email) {
        return documents.findByIdAndOwnerEmail(id, email)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Document not found."));
    }

    private String safeFilename(String original) {
        if (original == null || original.isBlank()) return "upload";
        String leaf = original.replace('\\', '/');
        leaf = leaf.substring(leaf.lastIndexOf('/') + 1).trim();
        return leaf.length() > 255 ? leaf.substring(0, 255) : leaf;
    }

    private String extension(String filename) {
        int dot = filename.lastIndexOf('.');
        return dot < 0 ? "" : filename.substring(dot + 1).toLowerCase(Locale.ROOT);
    }

    private String safeError(Exception ex) {
        String message = ex.getMessage();
        if (message == null || message.isBlank()) return "Document processing failed.";
        return message.length() > 480 ? message.substring(0, 480) : message;
    }
}
