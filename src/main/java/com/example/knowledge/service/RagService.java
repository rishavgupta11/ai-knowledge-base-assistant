package com.example.knowledge.service;

import com.example.knowledge.dto.AskRequest;
import com.example.knowledge.dto.AskResponse;
import com.example.knowledge.dto.SearchRequest;
import com.example.knowledge.dto.SearchResultResponse;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class RagService {

    private final SemanticSearchService searchService;
    private final OllamaChatService chatService;

    public RagService(
            SemanticSearchService searchService,
            OllamaChatService chatService) {

        this.searchService = searchService;
        this.chatService = chatService;
    }

    public AskResponse askQuestion(AskRequest request) {

        // 1. Retrieve relevant chunks using semantic search
        int topK = request.topK() == null ? 5 : request.topK();

        List<SearchResultResponse> sources =
                searchService.search(
                        new SearchRequest(request.question(), topK)
                );

        // 2. Handle the case where no relevant chunks exist
        if (sources.isEmpty()) {
            return new AskResponse(
                    request.question(),
                    "I could not find relevant information in the uploaded documents.",
                    sources
            );
        }

        // 3. Build context from retrieved chunks
        String context = sources.stream()
                .map(source -> """
                        Document ID: %d
                        Chunk ID: %d
                        Content:
                        %s
                        """.formatted(
                        source.documentId(),
                        source.chunkId(),
                        source.content()
                ))
                .collect(Collectors.joining("\n---\n"));

        // 4. Generate an answer using the retrieved context
        String answer = chatService.generateAnswer(
                request.question(),
                context
        );

        // 5. Return answer along with its sources
        return new AskResponse(
                request.question(),
                answer,
                sources
        );
    }
}
