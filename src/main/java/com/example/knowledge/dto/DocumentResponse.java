package com.example.knowledge.dto;

import java.time.LocalDateTime;

public record DocumentResponse(
        Long id,
        String fileName,
        String fileType,
        Long fileSize,
        String status,
        LocalDateTime createdAt
) {}