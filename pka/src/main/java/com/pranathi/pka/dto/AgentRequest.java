package com.pranathi.pka.dto;

import jakarta.validation.constraints.NotBlank;

public record AgentRequest(
        @NotBlank String task,
        @NotBlank String question) {
}
