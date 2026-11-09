package com.example.knowledge.dto;

public record DocumentUploadResponse(
        Long id,
        String fileName,
        String fileType,
        Long fileSize,
        String status,
        String message
) {
}
