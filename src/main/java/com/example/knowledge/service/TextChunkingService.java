package com.example.knowledge.service;

import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class TextChunkingService {

    private static final int CHUNK_SIZE = 1000;
    private static final int CHUNK_OVERLAP = 200;

    public List<String> chunkText(String text) {

        List<String> chunks = new ArrayList<>();

        // Handle null or empty input
        if (text == null || text.isBlank()) {
            return chunks;
        }

        int start = 0;
        int textLength = text.length();

        while (start < textLength) {

            // Calculate the end position of the current chunk
            int end = Math.min(start + CHUNK_SIZE, textLength);

            // Extract the current chunk
            String chunk = text.substring(start, end);

            if (!chunk.isBlank()) {
                chunks.add(chunk);
            }

            // Stop when the entire text has been processed
            if (end == textLength) {
                break;
            }

            // Move forward while retaining the overlapping characters
            start = end - CHUNK_OVERLAP;
        }

        return chunks;
    }
}
