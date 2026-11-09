package com.example.knowledge.dto;

public record SearchResultResponse(
        Long chunkId,
        Long documentId,
        Integer chunkIndex,
        String content,
        Double similarityScore
) {}
