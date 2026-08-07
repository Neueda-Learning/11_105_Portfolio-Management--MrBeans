package com.portfoliomanager.controller;

import com.portfoliomanager.service.ChatService;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.portfoliomanager.dto.chatbot.ChatRequest;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ChatControllerTest {

    @Mock private ChatService chatService;

    private ChatController controller;

    @BeforeEach
    void setUp() {
        controller = new ChatController(chatService);
    }

    @Test
    void chat_DelegatesToServiceAndWrapsResponse() {
        ChatRequest request = new ChatRequest();
        request.setMessage("How is my portfolio?");
        when(chatService.chat("How is my portfolio?")).thenReturn("Your portfolio is doing great!");

        Map<String, String> result = controller.chat(request);

        assertEquals("Your portfolio is doing great!", result.get("reply"));
        verify(chatService).chat("How is my portfolio?");
    }
}
