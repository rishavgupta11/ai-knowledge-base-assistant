package com.example.knowledge.service;

import com.example.knowledge.dto.OllamaEmbeddingResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.util.List;
import java.util.Map;

@Service
public class OllamaEmbeddingService {

    private final RestClient restClient;
    private final String embeddingModel;

    public OllamaEmbeddingService(
            RestClient.Builder restClientBuilder,
            @Value("${ollama.base-url}") String baseUrl,
            @Value("${ollama.embedding-model}") String embeddingModel) {

        this.restClient = restClientBuilder
                .baseUrl(baseUrl)
                .build();

        this.embeddingModel = embeddingModel;
    }

    public List<Double> generateEmbedding(String text) {

        Map<String, Object> requestBody = Map.of(
                "model", embeddingModel,
                "input", text
        );

        OllamaEmbeddingResponse response = restClient.post()
                .uri("/api/embed")
                .body(requestBody)
                .retrieve()
                .body(OllamaEmbeddingResponse.class);

        if (response == null
                || response.embeddings() == null
                || response.embeddings().isEmpty()) {
            throw new IllegalStateException(
                    "Ollama returned an empty embedding"
            );
        }

        List<Double> embedding = response.embeddings().get(0);

        // Validate compatibility with VECTOR(768)
        if (embedding.size() != 768) {
            throw new IllegalStateException(
                    "Expected 768 dimensions but received "
                            + embedding.size()
            );
        }

        return embedding;
    }
}
