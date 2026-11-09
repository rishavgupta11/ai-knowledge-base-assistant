package com.example.knowledge.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;

public record AskRequest(

        @NotBlank(message = "Question cannot be empty")
        String question,

        @Positive(message = "Top K must be greater than zero")
        Integer topK

) {}
