package com.pranathi.pka.entity;

import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "chat_messages")
public class ChatMessage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(columnDefinition = "TEXT")
    private String content;

    public enum MessageRole {
    USER,
    ASSISTANT
}

@Enumerated(EnumType.STRING)
private MessageRole role;

private LocalDateTime createdAt = LocalDateTime.now();

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "session_id")
    private ChatSession session;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_message_id")
    private ChatMessage parentMessage;

    public ChatMessage() {
    }

    public Long getId() {
        return id;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

public MessageRole getRole() {
    return role;
}

public void setRole(MessageRole role) {
    this.role = role;
}

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public ChatSession getSession() {
        return session;
    }

    public void setSession(ChatSession session) {
        this.session = session;
    }

    public ChatMessage getParentMessage() {
        return parentMessage;
    }

    public void setParentMessage(ChatMessage parentMessage) {
        this.parentMessage = parentMessage;
    }
}