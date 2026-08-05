package com.portfoliomanager.controller;

import com.portfoliomanager.services.ChatService;
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
    public Map<String, String> chat(@RequestBody Map<String, String> request) {
        String userMessage = request.getOrDefault("message", "");
        String response = chatService.chat(userMessage);
        return Map.of("response", response);
    }
}
