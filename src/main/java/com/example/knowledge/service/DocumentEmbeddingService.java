package com.example.knowledge.service;

import com.example.knowledge.enums.DocumentStatus;
import com.example.knowledge.repository.DocumentChunkRepository;
import com.example.knowledge.repository.DocumentRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
public class DocumentEmbeddingService {

    private final DocumentChunkRepository chunkRepository;
    private final DocumentRepository documentRepository;
    private final OllamaEmbeddingService embeddingService;

    public DocumentEmbeddingService(
            DocumentChunkRepository chunkRepository,
            DocumentRepository documentRepository,
            OllamaEmbeddingService embeddingService) {

        this.chunkRepository = chunkRepository;
        this.documentRepository = documentRepository;
        this.embeddingService = embeddingService;
    }

    public int generateAndStoreEmbeddings(Long documentId) {

        // Fetch all chunks for this document
        List<Map<String, Object>> chunks =
                chunkRepository.findChunksByDocumentId(documentId);

        if (chunks.isEmpty()) {
            throw new IllegalStateException(
                    "No chunks found for document ID: " + documentId
            );
        }

        // Mark document as processing
        documentRepository.updateStatus(
                documentId,
                DocumentStatus.PROCESSING
        );

        try {
            int processedCount = 0;

            for (Map<String, Object> chunk : chunks) {

                Long chunkId =
                        ((Number) chunk.get("id")).longValue();

                String content =
                        (String) chunk.get("content");

                // Generate embedding using Ollama
                List<Double> embedding =
                        embeddingService.generateEmbedding(content);

                // Store embedding in PostgreSQL
                chunkRepository.updateEmbedding(
                        chunkId,
                        embedding
                );

                processedCount++;
            }

            // Mark document as successfully processed
            documentRepository.updateStatus(
                    documentId,
                    DocumentStatus.PROCESSED
            );

            return processedCount;

        } catch (RuntimeException exception) {

            // Mark document as failed if embedding generation fails
            documentRepository.updateStatus(
                    documentId,
                    DocumentStatus.FAILED
            );

            throw exception;
        }
    }
}
