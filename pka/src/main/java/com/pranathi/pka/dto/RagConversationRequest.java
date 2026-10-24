package com.pranathi.pka.dto;

import jakarta.validation.constraints.Size;

public record RagConversationRequest(@Size(max = 200) String title) {}
