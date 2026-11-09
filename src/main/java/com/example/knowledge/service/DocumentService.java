package com.example.knowledge.service;

import com.example.knowledge.dto.DocumentResponse;
import com.example.knowledge.dto.DocumentUploadResponse;
import com.example.knowledge.repository.DocumentRepository;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;


@Service
public class DocumentService {

    private final DocumentRepository documentRepository;

    @Value("${app.file.upload-dir}")
    private String uploadDirectory;

    private static final long MAX_FILE_SIZE = 10 * 1024 * 1024;

    public DocumentService(DocumentRepository documentRepository) {
        this.documentRepository = documentRepository;
    }

    public List<DocumentResponse> getAllDocuments() {
        return documentRepository.findAllDocuments();
    }

    public DocumentResponse getDocumentById(Long id) {
        return documentRepository.findDocumentResponseById(id);
    }

    public DocumentUploadResponse upload(MultipartFile file) {

        // 1. Check whether file is empty
        if (file == null || file.isEmpty()) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Please select a file"
            );
        }

        // 2. Validate file size
        if (file.getSize() > MAX_FILE_SIZE) {
            throw new ResponseStatusException(
                    HttpStatus.PAYLOAD_TOO_LARGE,
                    "File size must not exceed 10 MB"
            );
        }

        // 3. Get original filename
        String originalFileName = file.getOriginalFilename();

        if (originalFileName == null || originalFileName.isBlank()) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Invalid file name"
            );
        }

        // Prevent path traversal through the supplied filename
        String fileName = originalFileName
                .replace("\\", "/");

        fileName = fileName.substring(
                fileName.lastIndexOf('/') + 1
        );

        // 4. Validate extension
        String lowerFileName = fileName.toLowerCase(Locale.ROOT);

        String fileType;

        if (lowerFileName.endsWith(".pdf")) {
            fileType = "PDF";
        } else if (lowerFileName.endsWith(".txt")) {
            fileType = "TXT";
        } else {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Only PDF and TXT files are allowed"
            );
        }

        // 5. Validate PDF signature
        if (fileType.equals("PDF")) {
            try {
                byte[] header = file.getBytes();

                if (header.length < 5 ||
                        header[0] != '%' ||
                        header[1] != 'P' ||
                        header[2] != 'D' ||
                        header[3] != 'F' ||
                        header[4] != '-') {

                    throw new ResponseStatusException(
                            HttpStatus.BAD_REQUEST,
                            "Invalid PDF file"
                    );
                }

            } catch (IOException e) {
                throw new ResponseStatusException(
                        HttpStatus.INTERNAL_SERVER_ERROR,
                        "Unable to read uploaded file"
                );
            }
        }

        // 6. Create storage directory
        Path uploadPath = Path.of(uploadDirectory)
                .toAbsolutePath()
                .normalize();

        String storedFileName = UUID.randomUUID() +
                (fileType.equals("PDF") ? ".pdf" : ".txt");

        Path destination = uploadPath
                .resolve(storedFileName)
                .normalize();

        if (!destination.startsWith(uploadPath)) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Invalid file path"
            );
        }

        boolean fileSaved = false;

        try {
            Files.createDirectories(uploadPath);

            // 7. Save physical file
            Files.copy(
                    file.getInputStream(),
                    destination,
                    StandardCopyOption.REPLACE_EXISTING
            );

            fileSaved = true;

            // 8. Save metadata in PostgreSQL
            Long documentId = documentRepository.save(
                    fileName,
                    fileType,
                    file.getSize(),
                    destination.toString()
            );

            // 9. Return response
            return new DocumentUploadResponse(
                    documentId,
                    fileName,
                    fileType,
                    file.getSize(),
                    "UPLOADED",
                    "Document uploaded successfully"
            );

        } catch (IOException | RuntimeException e) {

            // Remove saved file if database insertion fails
            if (fileSaved) {
                try {
                    Files.deleteIfExists(destination);
                } catch (IOException cleanupError) {
                    e.addSuppressed(cleanupError);
                }
            }

            if (e instanceof ResponseStatusException responseException) {
                throw responseException;
            }

            throw new ResponseStatusException(
                    HttpStatus.INTERNAL_SERVER_ERROR,
                    "Document upload failed",
                    e
            );
        }
    }

    public void deleteDocument(Long id) {

        Map<String, Object> document = documentRepository.findDocumentById(id);

        String storagePath = (String) document.get("storage_path");

        try {
            if (storagePath != null) {
                Files.deleteIfExists(Path.of(storagePath));
            }
        } catch (IOException exception) {
            throw new RuntimeException("Failed to delete document file", exception);
        }

        documentRepository.deleteById(id);
    }
}