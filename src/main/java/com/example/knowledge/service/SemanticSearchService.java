package com.example.knowledge.service;

import com.example.knowledge.dto.SearchRequest;
import com.example.knowledge.dto.SearchResultResponse;
import com.example.knowledge.repository.DocumentChunkRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class SemanticSearchService {

    private final OllamaEmbeddingService embeddingService;
    private final DocumentChunkRepository chunkRepository;

    public SemanticSearchService(
            OllamaEmbeddingService embeddingService,
            DocumentChunkRepository chunkRepository) {

        this.embeddingService = embeddingService;
        this.chunkRepository = chunkRepository;
    }

    public List<SearchResultResponse> search(SearchRequest request) {

        // Use 5 results when topK is not provided
        int topK = request.topK() == null ? 5 : request.topK();

        // Generate an embedding for the user's question
        List<Double> queryEmbedding =
                embeddingService.generateEmbedding(request.query());

        // Find the most similar document chunks
        return chunkRepository.searchSimilarChunks(
                queryEmbedding,
                topK
        );
    }
}
