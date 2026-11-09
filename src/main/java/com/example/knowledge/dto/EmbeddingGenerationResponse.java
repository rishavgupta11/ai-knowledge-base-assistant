package com.example.knowledge.dto;

public record EmbeddingGenerationResponse(
        Long documentId,
        int totalEmbeddings,
        String status,
        String message
) {}