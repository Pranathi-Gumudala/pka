package com.pranathi.pka.dto;

import java.time.Instant;

public record RagConversationDto(Long id, String title, Instant createdAt, Instant updatedAt) {}
