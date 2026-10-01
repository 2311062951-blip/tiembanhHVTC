package com.hvtc.bakery.api;

import com.hvtc.bakery.dto.ChatRequest;
import com.hvtc.bakery.dto.ChatResponse;
import com.hvtc.bakery.service.ChatbotService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/chatbot")
@RequiredArgsConstructor
public class ChatbotApiController {

    private final ChatbotService chatbotService;

    @PostMapping("/message")
    public ResponseEntity<ChatResponse> chat(@RequestBody ChatRequest request) {
        ChatResponse response = chatbotService.processUserMessage(request);
        return ResponseEntity.ok(response);
    }
}
