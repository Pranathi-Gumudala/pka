package com.pranathi.pka.repository;

import com.pranathi.pka.entity.ChatSession;
import com.pranathi.pka.entity.Note;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ChatSessionRepository extends JpaRepository<ChatSession, Long> {

    List<ChatSession> findByNoteOrderByCreatedAtAsc(Note note);

}
