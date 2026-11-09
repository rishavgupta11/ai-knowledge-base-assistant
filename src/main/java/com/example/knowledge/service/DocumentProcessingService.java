package com.example.knowledge.service;

import com.example.knowledge.dto.DocumentProcessingResponse;
import com.example.knowledge.dto.DocumentTextResponse;
import com.example.knowledge.enums.DocumentStatus;
import com.example.knowledge.repository.DocumentChunkRepository;
import com.example.knowledge.repository.DocumentRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class DocumentProcessingService {

    private final DocumentExtractionService extractionService;
    private final TextChunkingService chunkingService;
    private final DocumentChunkRepository chunkRepository;
    private final DocumentRepository documentRepository;

    public DocumentProcessingService(
            DocumentExtractionService extractionService,
            TextChunkingService chunkingService,
            DocumentChunkRepository chunkRepository,
            DocumentRepository documentRepository) {

        this.extractionService = extractionService;
        this.chunkingService = chunkingService;
        this.chunkRepository = chunkRepository;
        this.documentRepository = documentRepository;
    }

    public DocumentProcessingResponse processDocument(Long documentId) {

        // Mark document as processing
        documentRepository.updateStatus(
                documentId,
                DocumentStatus.PROCESSING
        );

        try {
            // 1. Extract text from the document
            DocumentTextResponse extracted =
                    extractionService.extractText(documentId);

            // 2. Divide extracted text into chunks
            List<String> chunks =
                    chunkingService.chunkText(extracted.extractedText());

            if (chunks.isEmpty()) {
                throw new IllegalStateException(
                        "No readable text chunks found"
                );
            }

            // 3. Store chunks in PostgreSQL
            chunkRepository.replaceChunks(documentId, chunks);

            return new DocumentProcessingResponse(
                    documentId,
                    extracted.fileName(),
                    chunks.size(),
                    "PROCESSED",
                    "Document processed and chunks stored successfully"
            );

        } catch (RuntimeException exception) {

            // Mark document as failed
            documentRepository.updateStatus(
                    documentId,
                    DocumentStatus.FAILED
            );

            throw exception;
        }
    }
}
