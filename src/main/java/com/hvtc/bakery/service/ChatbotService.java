package com.hvtc.bakery.service;

import com.hvtc.bakery.dto.ChatRequest;
import com.hvtc.bakery.dto.ChatResponse;

public interface ChatbotService {
    ChatResponse processUserMessage(ChatRequest request);
}
