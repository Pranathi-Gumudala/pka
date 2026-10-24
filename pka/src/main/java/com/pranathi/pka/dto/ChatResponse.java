package com.pranathi.pka.dto;

import java.time.LocalDateTime;

public class ChatResponse {

    private Long messageId;

    private String role;

    private String content;

    private Long parentMessageId;

    private LocalDateTime createdAt;
    private Long sessionId;

    public ChatResponse() {
    }

    public Long getMessageId() {
        return messageId;
    }

    public void setMessageId(Long messageId) {
        this.messageId = messageId;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public Long getParentMessageId() {
        return parentMessageId;
    }

    public void setParentMessageId(Long parentMessageId) {
        this.parentMessageId = parentMessageId;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
    public Long getSessionId() {
    return sessionId;
}
public void setSessionId(Long sessionId) {
    this.sessionId = sessionId;
}
}