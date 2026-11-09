package com.example.knowledge.controller;

import com.example.knowledge.dto.*;
import com.example.knowledge.service.DocumentEmbeddingService;
import com.example.knowledge.service.DocumentExtractionService;
import com.example.knowledge.service.DocumentProcessingService;
import com.example.knowledge.service.DocumentService;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.List;

@RestController
@RequestMapping("/api/documents")
public class DocumentController {

    private final DocumentService documentService;

    private final DocumentExtractionService extractionService;

    private final DocumentProcessingService processingService;

    private final DocumentEmbeddingService embeddingService;

    public DocumentController(DocumentService documentService,
                              DocumentExtractionService extractionService,
                              DocumentProcessingService processingService,
                              DocumentEmbeddingService embeddingService) {
        this.documentService = documentService;
        this.extractionService = extractionService;
        this.processingService = processingService;
        this.embeddingService = embeddingService;
    }

    @PostMapping(
            value = "/upload",
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE
    )
    public ResponseEntity<DocumentUploadResponse> uploadDocument(
            @RequestParam("file") MultipartFile file
    ) {

        DocumentUploadResponse response =
                documentService.upload(file);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(response);
    }

    @PostMapping("/{id}/process")
    public ResponseEntity<DocumentProcessingResponse> processDocument(
            @PathVariable Long id) {

        DocumentProcessingResponse response =
                processingService.processDocument(id);

        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}/extract")
    public ResponseEntity<DocumentTextResponse> extractDocumentText(
            @PathVariable Long id
    ) {

        DocumentTextResponse response =
                extractionService.extractText(id);

        return ResponseEntity.ok(response);
    }

    @GetMapping
    public ResponseEntity<List<DocumentResponse>> getAllDocuments() {

        List<DocumentResponse> documents = documentService.getAllDocuments();

        return ResponseEntity.ok(documents);
    }

    @GetMapping("/{id}")
    public ResponseEntity<DocumentResponse> getDocumentById(
            @PathVariable Long id) {

        return ResponseEntity.ok(documentService.getDocumentById(id));
    }

    @PostMapping("/{id}/embed")
    public ResponseEntity<EmbeddingGenerationResponse> generateEmbeddings(
            @PathVariable Long id) {

        int totalEmbeddings =
                embeddingService.generateAndStoreEmbeddings(id);

        EmbeddingGenerationResponse response =
                new EmbeddingGenerationResponse(
                        id,
                        totalEmbeddings,
                        "COMPLETED",
                        "Embeddings generated and stored successfully"
                );

        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteDocument(@PathVariable Long id) {

        documentService.deleteDocument(id);

        return ResponseEntity.noContent().build();
    }
}