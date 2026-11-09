package com.example.knowledge.controller;
import com.example.knowledge.dto.AskRequest;
import com.example.knowledge.dto.AskResponse;
import com.example.knowledge.service.RagService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/ask")
public class AskController {

    private final RagService ragService;

    public AskController(RagService ragService) {
        this.ragService = ragService;
    }

    @PostMapping
    public ResponseEntity<AskResponse> askQuestion(
            @Valid @RequestBody AskRequest request) {

        AskResponse response = ragService.askQuestion(request);

        return ResponseEntity.ok(response);
    }
}

