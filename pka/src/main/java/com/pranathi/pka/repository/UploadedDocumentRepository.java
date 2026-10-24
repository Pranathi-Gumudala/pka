package com.pranathi.pka.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.pranathi.pka.entity.UploadedDocument;

public interface UploadedDocumentRepository extends JpaRepository<UploadedDocument, Long> {
    List<UploadedDocument> findByOwnerEmailOrderByCreatedAtDesc(String ownerEmail);
    Optional<UploadedDocument> findByIdAndOwnerEmail(Long id, String ownerEmail);
}
