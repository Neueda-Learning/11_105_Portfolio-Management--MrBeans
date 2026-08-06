package com.portfoliomanager.controller;

import com.portfoliomanager.dto.chatbot.ChatRequest;
import com.portfoliomanager.service.ChatService;

import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/chat")
public class ChatController {

    private final ChatService chatService;

    public ChatController(ChatService chatService) {
        this.chatService = chatService;
    }

    @PostMapping
    public Map<String, String> chat(@Valid @RequestBody ChatRequest request) {
        String userMessage = request.getMessage();
        String response = chatService.chat(userMessage);
        return Map.of("response", response);
    }
}
