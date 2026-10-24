package com.pranathi.pka.dto;

import jakarta.validation.constraints.NotBlank;

public record InternalRetrieveRequest(
        @NotBlank String query,
        @NotBlank String ownerEmail) {
}
