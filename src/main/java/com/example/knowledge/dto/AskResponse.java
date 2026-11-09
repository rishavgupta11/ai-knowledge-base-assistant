package com.example.knowledge.dto;

import java.util.List;

public record AskResponse(
        String question,
        String answer,
        List<SearchResultResponse> sources
) {}
