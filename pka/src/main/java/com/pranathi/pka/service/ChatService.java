package com.pranathi.pka.service;

import com.pranathi.pka.dto.ChatRequest;
import com.pranathi.pka.dto.ChatResponse;
import com.pranathi.pka.entity.ChatMessage;
import com.pranathi.pka.entity.ChatSession;
import com.pranathi.pka.entity.Note;
import com.pranathi.pka.repository.ChatMessageRepository;
import com.pranathi.pka.repository.ChatSessionRepository;
import com.pranathi.pka.repository.NoteRepository;
import org.springframework.stereotype.Service;

import java.util.List;
@Service
public class ChatService {

    private final AIService aiService;
    private final NoteRepository noteRepository;
    private final ChatSessionRepository sessionRepository;
    private final ChatMessageRepository messageRepository;

    public ChatService(
            AIService aiService,
            NoteRepository noteRepository,
            ChatSessionRepository sessionRepository,
            ChatMessageRepository messageRepository
    ) {
        this.aiService = aiService;
        this.noteRepository = noteRepository;
        this.sessionRepository = sessionRepository;
        this.messageRepository = messageRepository;
    }
    private String buildConversation(ChatSession session) {

    List<ChatMessage> messages =
            messageRepository.findBySessionOrderByCreatedAtAsc(session);

    StringBuilder history = new StringBuilder();

    for (ChatMessage message : messages) {

        history.append(message.getRole())
               .append(": ")
               .append(message.getContent())
               .append("\n");

    }

    return history.toString();
}
private ChatSession getOrCreateSession(ChatRequest request) {

    if (request.getSessionId() != null) {
        return sessionRepository.findById(request.getSessionId())
                .orElseThrow(() -> new RuntimeException("Session not found"));
    }

    Note note = noteRepository.findById(request.getNoteId())
            .orElseThrow(() -> new RuntimeException("Notebook not found"));

    ChatSession session = new ChatSession();

    session.setNote(note);

    session.setTitle(note.getTitle());

    return sessionRepository.save(session);

}
public ChatResponse sendMessage(ChatRequest request) {

    // Get existing session or create a new one
    ChatSession session = getOrCreateSession(request);

    // Find the notebook
    Note note = session.getNote();

    // Find parent message (for sub-chat)
    ChatMessage parent = null;

    if (request.getParentMessageId() != null) {
        parent = messageRepository.findById(request.getParentMessageId())
                .orElseThrow(() -> new RuntimeException("Parent message not found"));
    }

    // Save user message
    ChatMessage userMessage = new ChatMessage();

    userMessage.setSession(session);
    userMessage.setParentMessage(parent);
    userMessage.setRole(ChatMessage.MessageRole.USER);
    userMessage.setContent(request.getQuestion());

    messageRepository.save(userMessage);

    // Build conversation history
    String history = buildConversation(session);

    // Ask Gemini
    String answer = aiService.askNotebook(
            note.getContent(),
            history,
            request.getQuestion()
    );

    // Save AI message
    ChatMessage aiMessage = new ChatMessage();

    aiMessage.setSession(session);
    aiMessage.setParentMessage(parent);
    aiMessage.setRole(ChatMessage.MessageRole.ASSISTANT);
    aiMessage.setContent(answer);

    messageRepository.save(aiMessage);

    // Response
    ChatResponse response = new ChatResponse();

    response.setMessageId(aiMessage.getId());
    response.setRole(aiMessage.getRole().name());
    response.setContent(aiMessage.getContent());

    if (parent != null) {
        response.setParentMessageId(parent.getId());
    }

    response.setCreatedAt(aiMessage.getCreatedAt());
    response.setSessionId(session.getId());
    return response;
}

}
