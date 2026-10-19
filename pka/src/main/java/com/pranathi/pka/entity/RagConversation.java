package com.pranathi.pka.entity;

import java.time.Instant;
import jakarta.persistence.*;

@Entity
@Table(name = "rag_conversations", indexes = @Index(name = "idx_rag_conversation_owner", columnList = "owner_email, updated_at"))
public class RagConversation {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "owner_email", nullable = false, length = 320)
    private String ownerEmail;

    @Column(nullable = false, length = 200)
    private String title = "New conversation";

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    protected RagConversation() {}

    public RagConversation(String ownerEmail, String title) {
        this.ownerEmail = ownerEmail;
        this.title = title == null || title.isBlank() ? "New conversation" : title.substring(0, Math.min(200, title.length()));
    }

    @PrePersist
    void onCreate() {
        Instant now = Instant.now();
        createdAt = now;
        updatedAt = now;
    }

    @PreUpdate
    void onUpdate() { updatedAt = Instant.now(); }

    public Long getId() { return id; }
    public String getOwnerEmail() { return ownerEmail; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public Instant getCreatedAt() { return createdAt; }
    public Instant getUpdatedAt() { return updatedAt; }
    public void touch() { updatedAt = Instant.now(); }
}
