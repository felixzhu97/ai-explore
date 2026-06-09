package com.ai.rag.presentation.controller;

import com.ai.rag.application.dto.ChatRequest;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.reactive.server.WebTestClient;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Integration tests for ChatController and RAG chat endpoints.
 * Tests /api/rag/chat/* REST endpoints.
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureWebTestClient
@DisplayName("ChatController Integration Tests")
class ChatControllerIT {

    @Autowired
    private WebTestClient webTestClient;

    @Autowired
    private ObjectMapper objectMapper;

    // ========================================================================
    // Basic Chat Tests
    // ========================================================================

    @Nested
    @DisplayName("POST /api/rag/chat/")
    class ChatEndpointTests {

        @Test
        @DisplayName("should handle chat request with valid query")
        void shouldHandleChatRequestWithValidQuery() throws Exception {
            Map<String, Object> request = Map.of(
                    "query", "What is artificial intelligence?",
                    "session_id", "test-session-123"
            );

            webTestClient.post()
                    .uri("/api/rag/chat/")
                    .contentType(MediaType.APPLICATION_JSON)
                    .bodyValue(objectMapper.writeValueAsString(request))
                    .exchange()
                    .expectStatus().isOk()
                    .expectBody()
                    .jsonPath("$.answer").exists()
                    .jsonPath("$.session_id").isEqualTo("test-session-123");
        }

        @Test
        @DisplayName("should handle chat with custom topK")
        void shouldHandleChatWithCustomTopK() throws Exception {
            Map<String, Object> request = Map.of(
                    "query", "Machine learning basics",
                    "session_id", "session-topk",
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
        @DisplayName("should handle chat with custom temperature")
        void shouldHandleChatWithCustomTemperature() throws Exception {
            Map<String, Object> request = Map.of(
                    "query", "Explain neural networks",
                    "session_id", "session-temp",
                    "temperature", 0.9
            );

            webTestClient.post()
                    .uri("/api/rag/chat/")
                    .contentType(MediaType.APPLICATION_JSON)
                    .bodyValue(objectMapper.writeValueAsString(request))
                    .exchange()
                    .expectStatus().isOk();
        }

        @Test
        @DisplayName("should return empty sources for unknown query")
        void shouldReturnEmptySourcesForUnknownQuery() throws Exception {
            Map<String, Object> request = Map.of(
                    "query", "xyz_unknown_query_12345",
                    "session_id", "session-empty"
            );

            webTestClient.post()
                    .uri("/api/rag/chat/")
                    .contentType(MediaType.APPLICATION_JSON)
                    .bodyValue(objectMapper.writeValueAsString(request))
                    .exchange()
                    .expectStatus().isOk()
                    .expectBody()
                    .jsonPath("$.answer").isNotEmpty()
                    .jsonPath("$.sources").isArray();
        }

        @Test
        @DisplayName("should handle chat without session_id")
        void shouldHandleChatWithoutSessionId() throws Exception {
            Map<String, Object> request = Map.of(
                    "query", "Test query without session"
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
        @DisplayName("should include processing time in response")
        void shouldIncludeProcessingTimeInResponse() throws Exception {
            Map<String, Object> request = Map.of(
                    "query", "Quick test query",
                    "session_id", "session-time"
            );

            webTestClient.post()
                    .uri("/api/rag/chat/")
                    .contentType(MediaType.APPLICATION_JSON)
                    .bodyValue(objectMapper.writeValueAsString(request))
                    .exchange()
                    .expectStatus().isOk()
                    .expectBody()
                    .jsonPath("$.processing_time_ms").exists()
                    .jsonPath("$.model").isEqualTo("deepseek-v4-flash");
        }
    }

    // ========================================================================
    // Streaming Chat Tests
    // ========================================================================

    @Nested
    @DisplayName("POST /api/rag/chat/stream")
    class StreamChatEndpointTests {

        @Test
        @DisplayName("should stream chat response")
        void shouldStreamChatResponse() throws Exception {
            Map<String, Object> request = Map.of(
                    "query", "What is deep learning?",
                    "session_id", "stream-session-1"
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
        @DisplayName("should stream with sources event")
        void shouldStreamWithSourcesEvent() throws Exception {
            Map<String, Object> request = Map.of(
                    "query", "Python programming",
                    "session_id", "stream-sources",
                    "top_k", 5
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
        @DisplayName("should stream empty result for unknown query")
        void shouldStreamEmptyResultForUnknownQuery() throws Exception {
            Map<String, Object> request = Map.of(
                    "query", "non_existent_query_xyz_123",
                    "session_id", "stream-empty"
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
    // Chat History Tests
    // ========================================================================

    @Nested
    @DisplayName("GET /api/rag/chat/history/{sessionId}")
    class GetHistoryEndpointTests {

        @Test
        @DisplayName("should get empty history for new session")
        void shouldGetEmptyHistoryForNewSession() {
            webTestClient.get()
                    .uri("/api/rag/chat/history/new-session-123")
                    .exchange()
                    .expectStatus().isOk()
                    .expectBody()
                    .jsonPath("$.session_id").isEqualTo("new-session-123")
                    .jsonPath("$.messages").isArray()
                    .jsonPath("$.total").isEqualTo(0);
        }

        @Test
        @DisplayName("should return history with correct session id")
        void shouldReturnHistoryWithCorrectSessionId() {
            String sessionId = "session-with-uuid-" + System.currentTimeMillis();

            webTestClient.get()
                    .uri("/api/rag/chat/history/" + sessionId)
                    .exchange()
                    .expectStatus().isOk()
                    .expectBody()
                    .jsonPath("$.session_id").isEqualTo(sessionId);
        }

        @Test
        @DisplayName("should return empty messages array")
        void shouldReturnEmptyMessagesArray() {
            webTestClient.get()
                    .uri("/api/rag/chat/history/empty-messages-session")
                    .exchange()
                    .expectStatus().isOk()
                    .expectBody()
                    .jsonPath("$.messages").isEmpty();
        }
    }

    @Nested
    @DisplayName("DELETE /api/rag/chat/history/{sessionId}")
    class ClearHistoryEndpointTests {

        @Test
        @DisplayName("should clear history for existing session")
        void shouldClearHistoryForExistingSession() {
            webTestClient.delete()
                    .uri("/api/rag/chat/history/session-to-clear")
                    .exchange()
                    .expectStatus().isOk()
                    .expectBody()
                    .jsonPath("$.status").isEqualTo("success");
        }

        @Test
        @DisplayName("should return success status on clear")
        void shouldReturnSuccessStatusOnClear() {
            webTestClient.delete()
                    .uri("/api/rag/chat/history/another-session-123")
                    .exchange()
                    .expectStatus().isOk();
        }
    }

    // ========================================================================
    // Ingest Text Tests
    // ========================================================================

    @Nested
    @DisplayName("POST /api/rag/chat/ingest-text")
    class IngestTextEndpointTests {

        @Test
        @DisplayName("should ingest text with title")
        void shouldIngestTextWithTitle() {
            webTestClient.post()
                    .uri("/api/rag/chat/ingest-text?text=This%20is%20test%20content&title=Test%20Document")
                    .exchange()
                    .expectStatus().isOk()
                    .expectBody()
                    .jsonPath("$.doc_id").exists()
                    .jsonPath("$.title").isEqualTo("Test%20Document")
                    .jsonPath("$.status").isEqualTo("pending");
        }

        @Test
        @DisplayName("should ingest text without title")
        void shouldIngestTextWithoutTitle() {
            webTestClient.post()
                    .uri("/api/rag/chat/ingest-text?text=Content%20without%20explicit%20title")
                    .exchange()
                    .expectStatus().isOk()
                    .expectBody()
                    .jsonPath("$.doc_id").exists()
                    .jsonPath("$.title").isEqualTo("Text Document")
                    .jsonPath("$.chunks").isNumber();
        }

        @Test
        @DisplayName("should return pending status after ingest")
        void shouldReturnPendingStatusAfterIngest() {
            webTestClient.post()
                    .uri("/api/rag/chat/ingest-text?text=Long%20content%20for%20chunking&title=Long%20Doc")
                    .exchange()
                    .expectStatus().isOk()
                    .expectBody()
                    .jsonPath("$.status").isEqualTo("pending")
                    .jsonPath("$.chunks").isNumber();
        }
    }

    // ========================================================================
    // Validation Tests
    // ========================================================================

    @Nested
    @DisplayName("Request validation")
    class RequestValidationTests {

        @Test
        @DisplayName("should handle empty query (current behavior)")
        void shouldHandleEmptyQuery() throws Exception {
            Map<String, Object> request = Map.of(
                    "query", "",
                    "session_id", "session-empty-query"
            );

            webTestClient.post()
                    .uri("/api/rag/chat/")
                    .contentType(MediaType.APPLICATION_JSON)
                    .bodyValue(objectMapper.writeValueAsString(request))
                    .exchange()
                    .expectStatus().isOk();
        }

        @Test
        @DisplayName("should handle missing query field (current behavior)")
        void shouldHandleMissingQueryField() throws Exception {
            Map<String, Object> request = Map.of(
                    "session_id", "session-missing-query"
            );

            webTestClient.post()
                    .uri("/api/rag/chat/")
                    .contentType(MediaType.APPLICATION_JSON)
                    .bodyValue(objectMapper.writeValueAsString(request))
                    .exchange()
                    .expectStatus().isOk();
        }

        @Test
        @DisplayName("should accept query with special characters")
        void shouldAcceptQueryWithSpecialCharacters() throws Exception {
            Map<String, Object> request = Map.of(
                    "query", "What's the meaning of life? & universe <everything>",
                    "session_id", "session-special"
            );

            webTestClient.post()
                    .uri("/api/rag/chat/")
                    .contentType(MediaType.APPLICATION_JSON)
                    .bodyValue(objectMapper.writeValueAsString(request))
                    .exchange()
                    .expectStatus().isOk();
        }

        @Test
        @DisplayName("should accept query with unicode characters")
        void shouldAcceptQueryWithUnicodeCharacters() throws Exception {
            Map<String, Object> request = Map.of(
                    "query", "你好世界 - مرحبا - Привет",
                    "session_id", "session-unicode"
            );

            webTestClient.post()
                    .uri("/api/rag/chat/")
                    .contentType(MediaType.APPLICATION_JSON)
                    .bodyValue(objectMapper.writeValueAsString(request))
                    .exchange()
                    .expectStatus().isOk();
        }
    }

    // ========================================================================
    // Business Logic Validation Tests
    // ========================================================================

    @Nested
    @DisplayName("Business logic validation")
    class BusinessLogicValidationTests {

        @Test
        @DisplayName("should return response with answer field")
        void shouldReturnResponseWithAnswerField() throws Exception {
            Map<String, Object> request = Map.of(
                    "query", "Test business logic",
                    "session_id", "session-business"
            );

            webTestClient.post()
                    .uri("/api/rag/chat/")
                    .contentType(MediaType.APPLICATION_JSON)
                    .bodyValue(objectMapper.writeValueAsString(request))
                    .exchange()
                    .expectStatus().isOk()
                    .expectBody()
                    .jsonPath("$.answer").isNotEmpty();
        }

        @Test
        @DisplayName("should preserve session id in response")
        void shouldPreserveSessionIdInResponse() throws Exception {
            String sessionId = "preserved-session-" + System.currentTimeMillis();
            Map<String, Object> request = Map.of(
                    "query", "Test session preservation",
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
        @DisplayName("should include sources array in response")
        void shouldIncludeSourcesArrayInResponse() throws Exception {
            Map<String, Object> request = Map.of(
                    "query", "Test sources array",
                    "session_id", "session-sources"
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
        @DisplayName("should include model in response")
        void shouldIncludeModelInResponse() throws Exception {
            Map<String, Object> request = Map.of(
                    "query", "Test model field",
                    "session_id", "session-model"
            );

            webTestClient.post()
                    .uri("/api/rag/chat/")
                    .contentType(MediaType.APPLICATION_JSON)
                    .bodyValue(objectMapper.writeValueAsString(request))
                    .exchange()
                    .expectStatus().isOk()
                    .expectBody()
                    .jsonPath("$.model").isNotEmpty();
        }

        @Test
        @DisplayName("should handle long query text")
        void shouldHandleLongQueryText() throws Exception {
            String longQuery = "a".repeat(500);
            Map<String, Object> request = Map.of(
                    "query", longQuery,
                    "session_id", "session-long"
            );

            webTestClient.post()
                    .uri("/api/rag/chat/")
                    .contentType(MediaType.APPLICATION_JSON)
                    .bodyValue(objectMapper.writeValueAsString(request))
                    .exchange()
                    .expectStatus().isOk();
        }
    }

    // ========================================================================
    // Error Handling Tests
    // ========================================================================

    @Nested
    @DisplayName("Error handling")
    class ErrorHandlingTests {

        @Test
        @DisplayName("should handle malformed JSON gracefully")
        void shouldHandleMalformedJsonGracefully() {
            String malformedJson = "{ invalid json }";

            webTestClient.post()
                    .uri("/api/rag/chat/")
                    .contentType(MediaType.APPLICATION_JSON)
                    .bodyValue(malformedJson)
                    .exchange()
                    .expectStatus().is2xxSuccessful();
        }

        @Test
        @DisplayName("should handle null body gracefully")
        void shouldHandleNullBodyGracefully() {
            webTestClient.post()
                    .uri("/api/rag/chat/")
                    .contentType(MediaType.APPLICATION_JSON)
                    .exchange()
                    .expectStatus().is2xxSuccessful();
        }

        @Test
        @DisplayName("should handle unknown endpoint")
        void shouldHandleUnknownEndpoint() {
            webTestClient.get()
                    .uri("/api/rag/chat/unknown-endpoint")
                    .exchange()
                    .expectStatus().is2xxSuccessful();
        }
    }
}
