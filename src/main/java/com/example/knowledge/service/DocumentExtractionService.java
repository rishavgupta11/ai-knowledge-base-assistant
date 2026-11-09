package com.example.knowledge.service;

import com.example.knowledge.dto.DocumentTextResponse;
import com.example.knowledge.repository.DocumentRepository;

import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;

@Service
public class DocumentExtractionService {

    private final DocumentRepository documentRepository;

    public DocumentExtractionService(
            DocumentRepository documentRepository
    ) {
        this.documentRepository = documentRepository;
    }

    public DocumentTextResponse extractText(Long documentId) {

        // 1. Find document metadata from database
        Map<String, Object> document;

        try {
            document = documentRepository.findDocumentById(documentId);
        } catch (org.springframework.dao.EmptyResultDataAccessException e) {
            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND,
                    "Document not found"
            );
        }

        String fileName = (String) document.get("file_name");
        String fileType = (String) document.get("file_type");
        String storagePath = (String) document.get("storage_path");

        if (storagePath == null || storagePath.isBlank()) {
            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND,
                    "Document storage path not found"
            );
        }

        Path path = Path.of(storagePath);

        if (!Files.isRegularFile(path)) {
            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND,
                    "Uploaded file not found on disk"
            );
        }

        // 2. Extract text based on file type
        String extractedText;

        try {
            if ("PDF".equalsIgnoreCase(fileType)) {
                extractedText = extractPdfText(path);
            } else if ("TXT".equalsIgnoreCase(fileType)) {
                extractedText = Files.readString(
                        path,
                        StandardCharsets.UTF_8
                );
            } else {
                throw new ResponseStatusException(
                        HttpStatus.BAD_REQUEST,
                        "Unsupported document type"
                );
            }

        } catch (IOException e) {
            throw new ResponseStatusException(
                    HttpStatus.UNPROCESSABLE_ENTITY,
                    "Unable to extract document text",
                    e
            );
        }

        // 3. Validate extracted text
        if (extractedText.isBlank()) {
            throw new ResponseStatusException(
                    HttpStatus.UNPROCESSABLE_ENTITY,
                    "No readable text found in document"
            );
        }

        // 4. Return extraction result
        return new DocumentTextResponse(
                documentId,
                fileName,
                fileType,
                extractedText.length(),
                extractedText
        );
    }

    private String extractPdfText(Path path) throws IOException {

        // PDFBox 3.x uses Loader.loadPDF()
        try (PDDocument document = Loader.loadPDF(path.toFile())) {

            PDFTextStripper stripper = new PDFTextStripper();

            return stripper.getText(document);
        }
    }
}