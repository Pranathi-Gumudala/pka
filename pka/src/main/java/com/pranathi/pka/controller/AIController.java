package com.pranathi.pka.controller;

import com.pranathi.pka.dto.AIRequestDTO;
import com.pranathi.pka.dto.AIResponseDTO;
import com.pranathi.pka.service.AIService;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/ai")
public class AIController {

    private final AIService aiService;

    public AIController(AIService aiService) {
        this.aiService = aiService;
    }

    @PostMapping("/summarize")
    public AIResponseDTO summarize(@RequestBody AIRequestDTO request) {

        String response = aiService.summarize(request.getContent());

        return new AIResponseDTO(response);
    }
}
