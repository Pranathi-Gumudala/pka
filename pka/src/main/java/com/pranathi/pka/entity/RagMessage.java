package com.pranathi.pka.entity;

import java.time.Instant;
import jakarta.persistence.*;

@Entity
@Table(name = "rag_messages", indexes = {
    @Index(name = "idx_rag_message_conversation", columnList = "conversation_id, created_at"),
    @Index(name = "idx_rag_message_parent", columnList = "parent_message_id")
})
public class RagMessage {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "conversation_id", nullable = false)
    private RagConversation conversation;

    @Column(name = "parent_message_id")
    private Long parentMessageId;

    @Column(nullable = false, length = 20)
    private String role;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String content;

    @Column(name = "sources_json", columnDefinition = "TEXT")
    private String sourcesJson = "[]";

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    protected RagMessage() {}

    public RagMessage(RagConversation conversation, Long parentMessageId, String role, String content, String sourcesJson) {
        this.conversation = conversation;
        this.parentMessageId = parentMessageId;
        this.role = role;
        this.content = content;
        this.sourcesJson = sourcesJson == null ? "[]" : sourcesJson;
    }

    @PrePersist
    void onCreate() { createdAt = Instant.now(); }

    public Long getId() { return id; }
    public RagConversation getConversation() { return conversation; }
    public Long getParentMessageId() { return parentMessageId; }
    public String getRole() { return role; }
    public String getContent() { return content; }
    public String getSourcesJson() { return sourcesJson; }
    public Instant getCreatedAt() { return createdAt; }
}
