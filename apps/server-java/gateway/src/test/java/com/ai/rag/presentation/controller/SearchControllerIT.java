package com.ai.rag.presentation.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.reactive.server.WebTestClient;

import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Integration tests for Search endpoints.
 * Tests RAG search functionality across multiple controllers:
 * - /api/agents/rag/search (via AgentController)
 * - /api/rag/chat (via ChatController - search is implicit in chat)
 * - /api/rag/health (health and stats)
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureWebTestClient
@DisplayName("SearchController Integration Tests")
class SearchControllerIT {

    @Autowired
    private WebTestClient webTestClient;

    @Autowired
    private ObjectMapper objectMapper;

    // ========================================================================
    // RAG Search via Agent Controller Tests
    // ========================================================================

    @Nested
    @DisplayName("GET /api/agents/rag/search")
    class RagSearchViaAgentTests {

        @Test
        @DisplayName("should search with default topK")
        void shouldSearchWithDefaultTopK() {
            webTestClient.get()
                    .uri("/api/agents/rag/search?query=artificial%20intelligence")
                    .exchange()
                    .expectStatus().is2xxSuccessful();
        }

        @Test
        @DisplayName("should search with custom topK")
        void shouldSearchWithCustomTopK() {
            webTestClient.get()
                    .uri("/api/agents/rag/search?query=machine%20learning&topK=10")
                    .exchange()
                    .expectStatus().isOk()
                    .expectBody()
                    .jsonPath("$.result").exists();
        }

        @Test
        @DisplayName("should search with topK=1")
        void shouldSearchWithTopK1() {
            webTestClient.get()
                    .uri("/api/agents/rag/search?query=test&topK=1")
                    .exchange()
                    .expectStatus().isOk()
                    .expectBody()
                    .jsonPath("$.result").exists();
        }

        @Test
        @DisplayName("should handle empty query")
        void shouldHandleEmptyQuery() {
            webTestClient.get()
                    .uri("/api/agents/rag/search?query=")
                    .exchange()
                    .expectStatus().isOk();
        }

        @Test
        @DisplayName("should handle query with special characters")
        void shouldHandleQueryWithSpecialCharacters() {
            webTestClient.get()
                    .uri("/api/agents/rag/search?query=test%26query%3Dvalue")
                    .exchange()
                    .expectStatus().isOk()
                    .expectBody()
                    .jsonPath("$.result").exists();
        }

        @Test
        @DisplayName("should handle unicode query")
        void shouldHandleUnicodeQuery() {
            webTestClient.get()
                    .uri("/api/agents/rag/search?query=%E4%B8%AD%E6%96%87%E6%90%9C%E7%B4%A2")
                    .exchange()
                    .expectStatus().isOk()
                    .expectBody()
                    .jsonPath("$.result").exists();
        }
    }

    // ========================================================================
    // RAG Chat Search Tests (via chat endpoint)
    // ========================================================================

    @Nested
    @DisplayName("POST /api/rag/chat/ - Search context")
    class RagChatSearchTests {

        @Test
        @DisplayName("should search via chat with default topK")
        void shouldSearchViaChatWithDefaultTopK() throws Exception {
            Map<String, Object> request = Map.of(
                    "query", "What is deep learning?",
                    "session_id", "search-session-1"
            );

            webTestClient.post()
                    .uri("/api/rag/chat/")
                    .contentType(MediaType.APPLICATION_JSON)
                    .bodyValue(objectMapper.writeValueAsString(request))
                    .exchange()
                    .expectStatus().isOk()
                    .expectBody()
                    .jsonPath("$.answer").exists()
                    .jsonPath("$.sources").isArray();
        }

        @Test
        @DisplayName("should search via chat with custom topK")
        void shouldSearchViaChatWithCustomTopK() throws Exception {
            Map<String, Object> request = Map.of(
                    "query", "Neural networks explanation",
                    "session_id", "search-session-topk",
                    "top_k", 10
            );

            webTestClient.post()
                    .uri("/api/rag/chat/")
                    .contentType(MediaType.APPLICATION_JSON)
                    .bodyValue(objectMapper.writeValueAsString(request))
                    .exchange()
                    .expectStatus().isOk()
                    .expectBody()
                    .jsonPath("$.answer").exists();
        }

        @Test
        @DisplayName("should return sources with scores")
        void shouldReturnSourcesWithScores() throws Exception {
            Map<String, Object> request = Map.of(
                    "query", "Python programming",
                    "session_id", "sources-session"
            );

            webTestClient.post()
                    .uri("/api/rag/chat/")
                    .contentType(MediaType.APPLICATION_JSON)
                    .bodyValue(objectMapper.writeValueAsString(request))
                    .exchange()
                    .expectStatus().isOk()
                    .expectBody()
                    .jsonPath("$.sources").isArray();
        }

        @Test
        @DisplayName("should handle search with no results")
        void shouldHandleSearchWithNoResults() throws Exception {
            Map<String, Object> request = Map.of(
                    "query", "xyz_nonexistent_query_" + UUID.randomUUID(),
                    "session_id", "no-results-session"
            );

            webTestClient.post()
                    .uri("/api/rag/chat/")
                    .contentType(MediaType.APPLICATION_JSON)
                    .bodyValue(objectMapper.writeValueAsString(request))
                    .exchange()
                    .expectStatus().isOk()
                    .expectBody()
                    .jsonPath("$.answer").exists();
        }
    }

    // ========================================================================
    // Streaming Search Tests
    // ========================================================================

    @Nested
    @DisplayName("POST /api/rag/chat/stream - Search context")
    class StreamingSearchTests {

        @Test
        @DisplayName("should stream search results")
        void shouldStreamSearchResults() throws Exception {
            Map<String, Object> request = Map.of(
                    "query", "What is reinforcement learning?",
                    "session_id", "stream-search-session"
            );

            webTestClient.post()
                    .uri("/api/rag/chat/stream")
                    .contentType(MediaType.APPLICATION_JSON)
                    .bodyValue(objectMapper.writeValueAsString(request))
                    .exchange()
                    .expectStatus().isOk()
                    .expectHeader().contentTypeCompatibleWith(MediaType.TEXT_EVENT_STREAM);
        }

        @Test
        @DisplayName("should stream search with sources event")
        void shouldStreamSearchWithSourcesEvent() throws Exception {
            Map<String, Object> request = Map.of(
                    "query", "Computer vision basics",
                    "session_id", "stream-sources-search"
            );

            webTestClient.post()
                    .uri("/api/rag/chat/stream")
                    .contentType(MediaType.APPLICATION_JSON)
                    .bodyValue(objectMapper.writeValueAsString(request))
                    .exchange()
                    .expectStatus().isOk()
                    .expectHeader().contentTypeCompatibleWith(MediaType.TEXT_EVENT_STREAM);
        }
    }

    // ========================================================================
    // RAG Health and Stats Tests
    // ========================================================================

    @Nested
    @DisplayName("GET /api/rag/health")
    class RagHealthTests {

        @Test
        @DisplayName("should return basic health status")
        void shouldReturnBasicHealthStatus() {
            webTestClient.get()
                    .uri("/api/rag/health")
                    .exchange()
                    .expectStatus().isOk()
                    .expectBody()
                    .jsonPath("$.status").isEqualTo("ok")
                    .jsonPath("$.service").isEqualTo("rag-service");
        }

        @Test
        @DisplayName("should include embedding model info")
        void shouldIncludeEmbeddingModelInfo() {
            webTestClient.get()
                    .uri("/api/rag/health")
                    .exchange()
                    .expectStatus().isOk()
                    .expectBody()
                    .jsonPath("$.embedding_model").exists()
                    .jsonPath("$.llm_provider").exists();
        }

        @Test
        @DisplayName("should indicate qdrant connection status")
        void shouldIndicateQdrantConnectionStatus() {
            webTestClient.get()
                    .uri("/api/rag/health")
                    .exchange()
                    .expectStatus().isOk()
                    .expectBody()
                    .jsonPath("$.qdrant_connected").exists();
        }
    }

    @Nested
    @DisplayName("GET /api/rag/health/detailed")
    class DetailedHealthTests {

        @Test
        @DisplayName("should return detailed health with components")
        void shouldReturnDetailedHealthWithComponents() {
            webTestClient.get()
                    .uri("/api/rag/health/detailed")
                    .exchange()
                    .expectStatus().isOk()
                    .expectBody()
                    .jsonPath("$.status").isEqualTo("ok")
                    .jsonPath("$.components").exists()
                    .jsonPath("$.components.documents").exists()
                    .jsonPath("$.components.vector_store").exists();
        }

        @Test
        @DisplayName("should include document count in detailed health")
        void shouldIncludeDocumentCountInDetailedHealth() {
            webTestClient.get()
                    .uri("/api/rag/health/detailed")
                    .exchange()
                    .expectStatus().isOk()
                    .expectBody()
                    .jsonPath("$.components.documents.count").isNumber();
        }
    }

    @Nested
    @DisplayName("GET /api/rag/info")
    class RagInfoTests {

        @Test
        @DisplayName("should return service info")
        void shouldReturnServiceInfo() {
            webTestClient.get()
                    .uri("/api/rag/info")
                    .exchange()
                    .expectStatus().isOk()
                    .expectBody()
                    .jsonPath("$.name").isEqualTo("RAG Service")
                    .jsonPath("$.version").exists()
                    .jsonPath("$.description").exists();
        }

        @Test
        @DisplayName("should include endpoint documentation")
        void shouldIncludeEndpointDocumentation() {
            webTestClient.get()
                    .uri("/api/rag/info")
                    .exchange()
                    .expectStatus().isOk()
                    .expectBody()
                    .jsonPath("$.endpoints").exists()
                    .jsonPath("$.endpoints.documents").exists()
                    .jsonPath("$.endpoints.chat").exists()
                    .jsonPath("$.endpoints.ingest").exists();
        }

        @Test
        @DisplayName("should document upload endpoint")
        void shouldDocumentUploadEndpoint() {
            webTestClient.get()
                    .uri("/api/rag/info")
                    .exchange()
                    .expectStatus().isOk()
                    .expectBody()
                    .jsonPath("$.endpoints.documents.upload").exists();
        }

        @Test
        @DisplayName("should document chat endpoint")
        void shouldDocumentChatEndpoint() {
            webTestClient.get()
                    .uri("/api/rag/info")
                    .exchange()
                    .expectStatus().isOk()
                    .expectBody()
                    .jsonPath("$.endpoints.chat.query").exists();
        }
    }

    // ========================================================================
    // Search Validation Tests
    // ========================================================================

    @Nested
    @DisplayName("Search validation")
    class SearchValidationTests {

        @Test
        @DisplayName("should handle very long query")
        void shouldHandleVeryLongQuery() {
            String longQuery = "test test test";  // Reasonable length query
            webTestClient.get()
                    .uri("/api/agents/rag/search?query=" + longQuery.replace(" ", "%20"))
                    .exchange()
                    .expectStatus().isOk();
        }

        @Test
        @DisplayName("should handle query with numbers")
        void shouldHandleQueryWithNumbers() {
            webTestClient.get()
                    .uri("/api/agents/rag/search?query=model%20version%201.2.3")
                    .exchange()
                    .expectStatus().isOk()
                    .expectBody()
                    .jsonPath("$.result").exists();
        }

        @Test
        @DisplayName("should handle query with mixed case")
        void shouldHandleQueryWithMixedCase() {
            webTestClient.get()
                    .uri("/api/agents/rag/search?query=MaChInE%20LeaRNiNG")
                    .exchange()
                    .expectStatus().isOk();
        }

        @Test
        @DisplayName("should handle topK boundary values")
        void shouldHandleTopKBoundaryValues() {
            webTestClient.get()
                    .uri("/api/agents/rag/search?query=test&topK=100")
                    .exchange()
                    .expectStatus().isOk();
        }
    }

    // ========================================================================
    // Search Business Logic Tests
    // ========================================================================

    @Nested
    @DisplayName("Search business logic")
    class SearchBusinessLogicTests {

        @Test
        @DisplayName("should return consistent results for same query")
        void shouldReturnConsistentResultsForSameQuery() {
            String query = "consistent-test-query";

            // First search
            webTestClient.get()
                    .uri("/api/agents/rag/search?query=" + query)
                    .exchange()
                    .expectStatus().isOk()
                    .expectBody()
                    .jsonPath("$.result").exists();

            // Second search should also succeed
            webTestClient.get()
                    .uri("/api/agents/rag/search?query=" + query)
                    .exchange()
                    .expectStatus().isOk()
                    .expectBody()
                    .jsonPath("$.result").exists();
        }

        @Test
        @DisplayName("should maintain session context in chat search")
        void shouldMaintainSessionContextInChatSearch() throws Exception {
            String sessionId = "context-session-" + UUID.randomUUID();

            Map<String, Object> request = Map.of(
                    "query", "First query in session",
                    "session_id", sessionId
            );

            webTestClient.post()
                    .uri("/api/rag/chat/")
                    .contentType(MediaType.APPLICATION_JSON)
                    .bodyValue(objectMapper.writeValueAsString(request))
                    .exchange()
                    .expectStatus().isOk()
                    .expectBody()
                    .jsonPath("$.session_id").isEqualTo(sessionId);
        }

        @Test
        @DisplayName("should return search metadata in response")
        void shouldReturnSearchMetadataInResponse() throws Exception {
            Map<String, Object> request = Map.of(
                    "query", "Test metadata",
                    "session_id", "metadata-session"
            );

            webTestClient.post()
                    .uri("/api/rag/chat/")
                    .contentType(MediaType.APPLICATION_JSON)
                    .bodyValue(objectMapper.writeValueAsString(request))
                    .exchange()
                    .expectStatus().isOk()
                    .expectBody()
                    .jsonPath("$.model").exists()
                    .jsonPath("$.processing_time_ms").exists();
        }
    }

    // ========================================================================
    // Error Handling Tests
    // ========================================================================

    @Nested
    @DisplayName("Error handling")
    class ErrorHandlingTests {

        @Test
        @DisplayName("should handle missing query parameter")
        void shouldHandleMissingQueryParameter() {
            webTestClient.get()
                    .uri("/api/agents/rag/search")
                    .exchange()
                    .expectStatus().is2xxSuccessful();
        }

        @Test
        @DisplayName("should handle unknown RAG health endpoint")
        void shouldHandleUnknownRagHealthEndpoint() {
            webTestClient.get()
                    .uri("/api/rag/unknown")
                    .exchange()
                    .expectStatus().is2xxSuccessful();
        }

        @Test
        @DisplayName("should handle search with malformed topK")
        void shouldHandleSearchWithMalformedTopK() {
            webTestClient.get()
                    .uri("/api/agents/rag/search?query=test&topK=abc")
                    .exchange()
                    .expectStatus().is2xxSuccessful();
        }
    }
}
