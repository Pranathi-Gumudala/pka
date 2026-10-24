package com.pranathi.pka.repository;

import com.pranathi.pka.entity.ChatMessage;
import com.pranathi.pka.entity.ChatSession;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ChatMessageRepository extends JpaRepository<ChatMessage, Long> {

    List<ChatMessage> findBySessionOrderByCreatedAtAsc(ChatSession session);

    List<ChatMessage> findByParentMessage(ChatMessage parentMessage);

}
