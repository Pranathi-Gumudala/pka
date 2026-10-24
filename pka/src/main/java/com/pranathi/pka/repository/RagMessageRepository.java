package com.pranathi.pka.repository;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import com.pranathi.pka.entity.RagMessage;

public interface RagMessageRepository extends JpaRepository<RagMessage, Long> {
    List<RagMessage> findByConversation_IdOrderByCreatedAtAsc(Long conversationId);
    Optional<RagMessage> findByIdAndConversation_Id(Long id, Long conversationId);
}
