package com.pranathi.pka.dto;

import java.time.Instant;
import java.util.List;

public record RagChatMessageDto(
    Long id,
    Long parentMessageId,
    String role,
    String content,
    List<RagSource> sources,
    Instant createdAt
) {}
