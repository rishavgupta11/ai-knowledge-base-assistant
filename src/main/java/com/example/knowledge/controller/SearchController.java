package com.example.knowledge.controller;

import com.example.knowledge.dto.SearchRequest;
import com.example.knowledge.dto.SearchResultResponse;
import com.example.knowledge.service.SemanticSearchService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/search")
public class SearchController {

    private final SemanticSearchService searchService;

    public SearchController(SemanticSearchService searchService) {
        this.searchService = searchService;
    }

    @PostMapping
    public ResponseEntity<List<SearchResultResponse>> search(
            @Valid @RequestBody SearchRequest request) {

        List<SearchResultResponse> results =
                searchService.search(request);

        return ResponseEntity.ok(results);
    }
}
