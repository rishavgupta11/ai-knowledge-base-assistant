package com.example.knowledge.repository;

import com.example.knowledge.dto.DocumentResponse;
import com.example.knowledge.enums.DocumentStatus;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import java.sql.PreparedStatement;
import java.util.List;
import java.util.Map;

@Repository
public class DocumentRepository {

    private final JdbcTemplate jdbcTemplate;

    public DocumentRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    // Save uploaded document metadata and return generated ID
    public Long save(
            String fileName,
            String fileType,
            Long fileSize,
            String storagePath) {

        String sql = """
                INSERT INTO documents
                (file_name, file_type, file_size, storage_path, status)
                VALUES (?, ?, ?, ?, ?)
                """;

        KeyHolder keyHolder = new GeneratedKeyHolder();

        jdbcTemplate.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(
                    sql,
                    new String[]{"id"}
            );

            ps.setString(1, fileName);
            ps.setString(2, fileType);
            ps.setLong(3, fileSize);
            ps.setString(4, storagePath);
            ps.setString(5, DocumentStatus.UPLOADED.name());

            return ps;

        }, keyHolder);

        Number key = keyHolder.getKey();

        if (key == null) {
            throw new IllegalStateException(
                    "Failed to generate document ID"
            );
        }

        return key.longValue();
    }

    // Find document by ID (used by existing services)
    public Map<String, Object> findDocumentById(Long id) {

        String sql = """
                SELECT id, file_name, file_type, storage_path, status
                FROM documents
                WHERE id = ?
                """;

        return jdbcTemplate.queryForMap(sql, id);
    }

    // Find document details by ID and map directly to DTO
    public DocumentResponse findDocumentResponseById(Long id) {

        String sql = """
                SELECT id, file_name, file_type, file_size, status, created_at
                FROM documents
                WHERE id = ?
                """;

        return jdbcTemplate.queryForObject(sql, (rs, rowNum) ->
                new DocumentResponse(
                        rs.getLong("id"),
                        rs.getString("file_name"),
                        rs.getString("file_type"),
                        rs.getLong("file_size"),
                        rs.getString("status"),
                        rs.getTimestamp("created_at").toLocalDateTime()
                ), id);
    }


    // Find all uploaded documents
    public List<DocumentResponse> findAllDocuments() {

        String sql = """
                SELECT id, file_name, file_type, file_size, status, created_at
                FROM documents
                ORDER BY created_at DESC
                """;

        return jdbcTemplate.query(sql, (rs, rowNum) ->
                new DocumentResponse(
                        rs.getLong("id"),
                        rs.getString("file_name"),
                        rs.getString("file_type"),
                        rs.getLong("file_size"),
                        rs.getString("status"),
                        rs.getTimestamp("created_at").toLocalDateTime()
                )
        );
    }

    // Update document processing status
    public void updateStatus(
            Long documentId,
            DocumentStatus status) {

        String sql = """
                UPDATE documents
                SET status = ?
                WHERE id = ?
                """;

        int updatedRows = jdbcTemplate.update(
                sql,
                status.name(),
                documentId
        );

        if (updatedRows == 0) {
            throw new IllegalArgumentException(
                    "Document not found: " + documentId
            );
        }
    }

    // Delete document metadata; associated chunks are deleted by CASCADE
    public int deleteById(Long id) {
        String sql = "DELETE FROM documents WHERE id = ?";
        return jdbcTemplate.update(sql, id);
    }
}
