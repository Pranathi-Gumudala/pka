package com.pranathi.pka.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record RagChatMessageRequest(
    @NotBlank(message = "Question cannot be blank")
    @Size(max = 2000, message = "Question must be at most 2000 characters")
    String question,
    Long parentMessageId
) {}
