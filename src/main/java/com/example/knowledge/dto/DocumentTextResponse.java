package com.example.knowledge.dto;

public record DocumentTextResponse(
        Long documentId,
        String fileName,
        String fileType,
        int characterCount,
        String extractedText
) {
}