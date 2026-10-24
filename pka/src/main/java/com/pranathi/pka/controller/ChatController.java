package com.pranathi.pka.controller;

import com.pranathi.pka.dto.ChatRequest;
import com.pranathi.pka.dto.ChatResponse;
import com.pranathi.pka.service.ChatService;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/chat")
@CrossOrigin(origins = "http://localhost:5173")
public class ChatController {

    private final ChatService chatService;

    public ChatController(ChatService chatService) {
        this.chatService = chatService;
    }

    @PostMapping("/send")
    public ChatResponse send(@RequestBody ChatRequest request) {
        return chatService.sendMessage(request);
    }
}