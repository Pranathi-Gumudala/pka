package com.pranathi.pka.repository;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import com.pranathi.pka.entity.RagConversation;

public interface RagConversationRepository extends JpaRepository<RagConversation, Long> {
    List<RagConversation> findByOwnerEmailOrderByUpdatedAtDesc(String ownerEmail);
    Optional<RagConversation> findByIdAndOwnerEmail(Long id, String ownerEmail);
}
