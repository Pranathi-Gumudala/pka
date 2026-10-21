package com.pranathi.pka.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record RagChatMessageRequest(
    @NotBlank(message = "Question cannot be blank")
    @Size(max = 2000, message = "Question must be at most 2000 characters")
    String question,
    Long parentMessageId,
    @Pattern(regexp = "ask|research|summarize|quiz", message = "Unsupported study task")
    String task
) {
    public RagChatMessageRequest(String question, Long parentMessageId) {
        this(question, parentMessageId, "ask");
    }
}
