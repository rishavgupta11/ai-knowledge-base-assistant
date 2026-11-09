package com.example.knowledge.dto;

public record DocumentProcessingResponse(
        Long documentId,
        String fileName,
        int totalChunks,
        String status,
        String message
) {}
