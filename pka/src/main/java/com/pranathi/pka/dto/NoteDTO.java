package com.pranathi.pka.dto;

import jakarta.validation.constraints.NotBlank;

public class NoteDTO {
    private Long id;
    @NotBlank(message = "Title cannot be empty")
    private String title;
        @NotBlank(message = "Content cannot be empty")
    private String content;

    // Getters and Setters
}