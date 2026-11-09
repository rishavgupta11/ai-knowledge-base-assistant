package com.example.knowledge.dto;

import java.util.List;

public record OllamaEmbeddingResponse(
        String model,
        List<List<Double>> embeddings
) {}
