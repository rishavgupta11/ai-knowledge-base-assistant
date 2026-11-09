package com.example.knowledge.exception;

import com.example.knowledge.dto.SearchRequest;
import jakarta.validation.Valid;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class GlobalExceptionHandlerTest {

    private MockMvc mockMvc;

    // Initialize MockMvc before each test
    @BeforeEach
    void setUp() {

        mockMvc = MockMvcBuilders
                .standaloneSetup(new TestController())
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    // Test 1: 404 Not Found
    @Test
    void shouldReturn404WhenDocumentNotFound() throws Exception {

        mockMvc.perform(get("/test/missing"))

                .andExpect(status().isNotFound())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.error").value("Not Found"))
                .andExpect(jsonPath("$.message")
                        .value("Requested resource not found"))
                .andExpect(jsonPath("$.path").value("/test/missing"));
    }

    // Test 2: 400 Bad Request for invalid path variable
    @Test
    void shouldReturn400WhenPathVariableIsInvalid() throws Exception {

        mockMvc.perform(get("/test/documents/abc"))

                .andExpect(status().isBadRequest())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.error").value("Bad Request"))
                .andExpect(jsonPath("$.message")
                        .value("Invalid value for parameter: id"))
                .andExpect(jsonPath("$.path")
                        .value("/test/documents/abc"));
    }

    // Test 3: 400 Bad Request for empty search query
    @Test
    void shouldReturn400WhenSearchQueryIsEmpty() throws Exception {

        mockMvc.perform(post("/test/search")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "query": "",
                                    "topK": 5
                                }
                                """))

                .andExpect(status().isBadRequest())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.error").value("Bad Request"))
                .andExpect(jsonPath("$.message")
                        .value("query: Search query cannot be empty"))
                .andExpect(jsonPath("$.path").value("/test/search"));
    }

    // Test 4: 500 Internal Server Error
    @Test
    void shouldReturn500WhenUnexpectedExceptionOccurs() throws Exception {

        mockMvc.perform(get("/test/error"))

                .andExpect(status().isInternalServerError())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.status").value(500))
                .andExpect(jsonPath("$.error")
                        .value("Internal Server Error"))
                .andExpect(jsonPath("$.message")
                        .value("An unexpected server error occurred"))
                .andExpect(jsonPath("$.path").value("/test/error"));
    }

    // Test controller: Simulates different exception scenarios
    @RestController
    static class TestController {

        // Simulate missing document
        @GetMapping("/test/missing")
        public void missingDocument() {
            throw new EmptyResultDataAccessException(1);
        }

        // Simulate invalid document ID conversion
        @GetMapping("/test/documents/{id}")
        public Long getDocument(@PathVariable Long id) {
            return id;
        }

        // Simulate request validation
        @PostMapping("/test/search")
        public String search(@Valid @RequestBody SearchRequest request) {
            return "Valid search request";
        }

        // Simulate unexpected server exception
        @GetMapping("/test/error")
        public void triggerServerError() {
            throw new RuntimeException("Simulated internal server error");
        }
    }
}
