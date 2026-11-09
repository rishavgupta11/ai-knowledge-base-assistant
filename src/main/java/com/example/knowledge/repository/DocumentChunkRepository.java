package com.example.knowledge.repository;

import com.example.knowledge.dto.SearchResultResponse;
import org.springframework.jdbc.core.BatchPreparedStatementSetter;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;

@Repository
public class DocumentChunkRepository {

    private final JdbcTemplate jdbcTemplate;

    public DocumentChunkRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public void updateEmbedding(Long chunkId, List<Double> embedding) {

        // Convert the Java vector into PostgreSQL vector format: [0.1,0.2,...]
        String vectorValue = embedding.stream()
                .map(String::valueOf)
                .collect(java.util.stream.Collectors.joining(",", "[", "]"));

        String sql = """
            UPDATE document_chunks
            SET embedding = ?::vector
            WHERE id = ?
            """;

        jdbcTemplate.update(sql, vectorValue, chunkId);
    }

    public List<Map<String, Object>> findChunksByDocumentId(Long documentId) {

        String sql = """
            SELECT id, content
            FROM document_chunks
            WHERE document_id = ?
            ORDER BY chunk_index
            """;

        return jdbcTemplate.queryForList(sql, documentId);
    }

    public List<SearchResultResponse> searchSimilarChunks(
            List<Double> embedding,
            int topK) {

        String vectorValue = embedding.stream()
                .map(String::valueOf)
                .collect(java.util.stream.Collectors.joining(",", "[", "]"));

        String sql = """
            SELECT
                id,
                document_id,
                chunk_index,
                content,
                1 - (embedding <=> ?::vector) AS similarity_score
            FROM document_chunks
            WHERE embedding IS NOT NULL
            ORDER BY embedding <=> ?::vector
            LIMIT ?
            """;

        return jdbcTemplate.query(
                sql,
                (rs, rowNum) -> new SearchResultResponse(
                        rs.getLong("id"),
                        rs.getLong("document_id"),
                        rs.getInt("chunk_index"),
                        rs.getString("content"),
                        rs.getDouble("similarity_score")
                ),
                vectorValue,
                vectorValue,
                topK
        );
    }

    @Transactional
    public void replaceChunks(Long documentId, List<String> chunks) {

        // Remove previous chunks if this document is processed again
        String deleteSql = """
                DELETE FROM document_chunks
                WHERE document_id = ?
                """;

        jdbcTemplate.update(deleteSql, documentId);

        if (chunks == null || chunks.isEmpty()) {
            return;
        }

        String insertSql = """
                INSERT INTO document_chunks
                (document_id, chunk_index, content)
                VALUES (?, ?, ?)
                """;

        // Insert all chunks in a batch
        jdbcTemplate.batchUpdate(insertSql, new BatchPreparedStatementSetter() {

            @Override
            public void setValues(PreparedStatement ps, int index)
                    throws SQLException {

                ps.setLong(1, documentId);
                ps.setInt(2, index);
                ps.setString(3, chunks.get(index));
            }

            @Override
            public int getBatchSize() {
                return chunks.size();
            }
        });
    }
}
