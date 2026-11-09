package com.example.knowledge.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;

public record SearchRequest(

        @NotBlank(message = "Search query cannot be empty")
        String query,

        @Positive(message = "Top K must be greater than zero")
        Integer topK

) {}
