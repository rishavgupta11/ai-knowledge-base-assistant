package com.example.knowledge.dto;

public record OllamaChatResponse(
        Message message
) {
    public record Message(
            String role,
            String content
    ) {}
}
