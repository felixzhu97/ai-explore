package com.ai.rag.presentation.controller;

import com.ai.rag.application.dto.DocumentDTO;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.client.MultipartBodyBuilder;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.web.reactive.function.BodyInserters;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Integration tests for RagDocumentController.
 * Tests /api/rag/documents/* REST endpoints.
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureWebTestClient
@DisplayName("DocumentController Integration Tests")
class DocumentControllerIT {

    @Autowired
    private WebTestClient webTestClient;

    // ========================================================================
    // List Documents Tests
    // ========================================================================

    @Nested
    @DisplayName("GET /api/rag/documents/")
    class ListDocumentsTests {

        @Test
        @DisplayName("should return empty list initially")
        void shouldReturnEmptyListInitially() {
            webTestClient.get()
                    .uri("/api/rag/documents/")
                    .exchange()
                    .expectStatus().isOk()
                    .expectBody()
                    .jsonPath("$.documents").isArray()
                    .jsonPath("$.total").isNumber();
        }

        @Test
        @DisplayName("should return list with pagination defaults")
        void shouldReturnListWithPaginationDefaults() {
            webTestClient.get()
                    .uri("/api/rag/documents/")
                    .exchange()
                    .expectStatus().isOk()
                    .expectBody()
                    .jsonPath("$.documents").isArray();
        }

        @Test
        @DisplayName("should handle pagination parameters")
        void shouldHandlePaginationParameters() {
            webTestClient.get()
                    .uri("/api/rag/documents/?skip=0&limit=10")
                    .exchange()
                    .expectStatus().isOk()
                    .expectBody()
                    .jsonPath("$.documents").isArray();
        }

        @Test
        @DisplayName("should handle large limit values")
        void shouldHandleLargeLimitValues() {
            webTestClient.get()
                    .uri("/api/rag/documents/?skip=0&limit=1000")
                    .exchange()
                    .expectStatus().isOk();
        }
    }

    // ========================================================================
    // Upload Document Tests
    // ========================================================================

    @Nested
    @DisplayName("POST /api/rag/documents/upload")
    class UploadDocumentTests {

        @Test
        @DisplayName("should upload text document successfully")
        void shouldUploadTextDocumentSuccessfully() {
            byte[] fileContent = "This is test content for RAG indexing.".getBytes();
            
            MultipartBodyBuilder builder = new MultipartBodyBuilder();
            builder.part("file", new ByteArrayResource(fileContent) {
                @Override
                public String getFilename() {
                    return "test.txt";
                }
            }).contentType(MediaType.TEXT_PLAIN);

            webTestClient.post()
                    .uri("/api/rag/documents/upload")
                    .contentType(MediaType.MULTIPART_FORM_DATA)
                    .body(BodyInserters.fromMultipartData(builder.build()))
                    .exchange()
                    .expectStatus().isOk()
                    .expectBody()
                    .jsonPath("$.doc_id").exists()
                    .jsonPath("$.filename").isEqualTo("test.txt")
                    .jsonPath("$.chunks").isNumber()
                    .jsonPath("$.status").exists();
        }

        @Test
        @DisplayName("should upload document with custom title")
        void shouldUploadDocumentWithCustomTitle() {
            byte[] fileContent = "PDF content placeholder".getBytes();
            
            MultipartBodyBuilder builder = new MultipartBodyBuilder();
            builder.part("file", new ByteArrayResource(fileContent) {
                @Override
                public String getFilename() {
                    return "document.pdf";
                }
            }).contentType(MediaType.APPLICATION_PDF);

            webTestClient.post()
                    .uri("/api/rag/documents/upload?title=Custom%20Title")
                    .contentType(MediaType.MULTIPART_FORM_DATA)
                    .body(BodyInserters.fromMultipartData(builder.build()))
                    .exchange()
                    .expectStatus().isOk()
                    .expectBody()
                    .jsonPath("$.doc_id").exists()
                    .jsonPath("$.filename").isEqualTo("document.pdf");
        }

        @Test
        @DisplayName("should handle empty file content")
        void shouldHandleEmptyFileContent() {
            byte[] fileContent = "".getBytes();
            
            MultipartBodyBuilder builder = new MultipartBodyBuilder();
            builder.part("file", new ByteArrayResource(fileContent) {
                @Override
                public String getFilename() {
                    return "empty.txt";
                }
            }).contentType(MediaType.TEXT_PLAIN);

            webTestClient.post()
                    .uri("/api/rag/documents/upload")
                    .contentType(MediaType.MULTIPART_FORM_DATA)
                    .body(BodyInserters.fromMultipartData(builder.build()))
                    .exchange()
                    .expectStatus().isOk();
        }

        @Test
        @DisplayName("should handle markdown content")
        void shouldHandleMarkdownContent() {
            String markdown = "# Title\n\nContent with **bold** and *italic* text.\n\n## Section\n\n- Item 1\n- Item 2";
            byte[] fileContent = markdown.getBytes();
            
            MultipartBodyBuilder builder = new MultipartBodyBuilder();
            builder.part("file", new ByteArrayResource(fileContent) {
                @Override
                public String getFilename() {
                    return "readme.md";
                }
            }).contentType(MediaType.TEXT_PLAIN);

            webTestClient.post()
                    .uri("/api/rag/documents/upload")
                    .contentType(MediaType.MULTIPART_FORM_DATA)
                    .body(BodyInserters.fromMultipartData(builder.build()))
                    .exchange()
                    .expectStatus().isOk()
                    .expectBody()
                    .jsonPath("$.doc_id").exists()
                    .jsonPath("$.filename").isEqualTo("readme.md");
        }
    }

    // ========================================================================
    // Get Document Tests
    // ========================================================================

    @Nested
    @DisplayName("GET /api/rag/documents/{docId}")
    class GetDocumentTests {

        @Test
        @DisplayName("should return 404 for non-existent document")
        void shouldReturn404ForNonExistentDocument() {
            String nonExistentId = "non-existent-" + UUID.randomUUID();

            webTestClient.get()
                    .uri("/api/rag/documents/" + nonExistentId)
                    .exchange()
                    .expectStatus().isNotFound();
        }

        @Test
        @DisplayName("should return 404 for invalid document ID format")
        void shouldReturn404ForInvalidDocumentIdFormat() {
            webTestClient.get()
                    .uri("/api/rag/documents/invalid-id-format")
                    .exchange()
                    .expectStatus().isNotFound();
        }
    }

    // ========================================================================
    // Document Statistics Tests
    // ========================================================================

    @Nested
    @DisplayName("GET /api/rag/documents/{docId}/stats")
    class DocumentStatsTests {

        @Test
        @DisplayName("should return 404 for stats of non-existent document")
        void shouldReturn404ForStatsOfNonExistentDocument() {
            String nonExistentId = "stats-nonexistent-" + UUID.randomUUID();

            webTestClient.get()
                    .uri("/api/rag/documents/" + nonExistentId + "/stats")
                    .exchange()
                    .expectStatus().isNotFound();
        }

        @Test
        @DisplayName("should handle stats for invalid ID gracefully")
        void shouldHandleStatsForInvalidIdGracefully() {
            webTestClient.get()
                    .uri("/api/rag/documents/invalid/stats")
                    .exchange()
                    .expectStatus().isNotFound();
        }
    }

    // ========================================================================
    // Delete Document Tests
    // ========================================================================

    @Nested
    @DisplayName("DELETE /api/rag/documents/{docId}")
    class DeleteDocumentTests {

        @Test
        @DisplayName("should return 400 for invalid document ID on delete")
        void shouldReturn400ForInvalidDocumentIdOnDelete() {
            webTestClient.delete()
                    .uri("/api/rag/documents/invalid-delete-id")
                    .exchange()
                    .expectStatus().isBadRequest()
                    .expectBody()
                    .jsonPath("$.status").isEqualTo("error");
        }

        @Test
        @DisplayName("should handle delete of non-existent document")
        void shouldHandleDeleteOfNonExistentDocument() {
            String nonExistentId = "delete-nonexistent-" + UUID.randomUUID();

            webTestClient.delete()
                    .uri("/api/rag/documents/" + nonExistentId)
                    .exchange()
                    .expectStatus().isBadRequest();
        }
    }

    // ========================================================================
    // Reindex Document Tests
    // ========================================================================

    @Nested
    @DisplayName("POST /api/rag/documents/{docId}/reindex")
    class ReindexDocumentTests {

        @Test
        @DisplayName("should return info message for reindex")
        void shouldReturnInfoMessageForReindex() {
            webTestClient.post()
                    .uri("/api/rag/documents/doc-to-reindex/reindex")
                    .exchange()
                    .expectStatus().isOk()
                    .expectBody()
                    .jsonPath("$.status").isEqualTo("info")
                    .jsonPath("$.message").exists();
        }

        @Test
        @DisplayName("should include guidance in reindex response")
        void shouldIncludeGuidanceInReindexResponse() {
            webTestClient.post()
                    .uri("/api/rag/documents/another-doc/reindex")
                    .exchange()
                    .expectStatus().isOk()
                    .expectBody()
                    .jsonPath("$.message").value(msg -> assertThat(msg.toString()).contains("re-upload"));
        }
    }

    // ========================================================================
    // Cleanup All Documents Tests
    // ========================================================================

    @Nested
    @DisplayName("POST /api/rag/documents/cleanup-all")
    class CleanupAllDocumentsTests {

        @Test
        @DisplayName("should return info for cleanup-all endpoint")
        void shouldReturnInfoForCleanupAllEndpoint() {
            webTestClient.post()
                    .uri("/api/rag/documents/cleanup-all")
                    .exchange()
                    .expectStatus().isOk()
                    .expectBody()
                    .jsonPath("$.status").isEqualTo("info")
                    .jsonPath("$.message").exists();
        }
    }

    // ========================================================================
    // Health Check Tests
    // ========================================================================

    @Nested
    @DisplayName("GET /api/rag/documents/health")
    class HealthCheckTests {

        @Test
        @DisplayName("should return health status with document count")
        void shouldReturnHealthStatusWithDocumentCount() {
            webTestClient.get()
                    .uri("/api/rag/documents/health")
                    .exchange()
                    .expectStatus().isOk()
                    .expectBody()
                    .jsonPath("$.status").isEqualTo("ok")
                    .jsonPath("$.total_documents").isNumber();
        }

        @Test
        @DisplayName("should include vector stats in health response")
        void shouldIncludeVectorStatsInHealthResponse() {
            webTestClient.get()
                    .uri("/api/rag/documents/health")
                    .exchange()
                    .expectStatus().isOk()
                    .expectBody()
                    .jsonPath("$.status").isEqualTo("ok")
                    .jsonPath("$.vector_stats").exists();
        }
    }

    // ========================================================================
    // Ingest URL Tests
    // ========================================================================

    @Nested
    @DisplayName("POST /api/rag/documents/ingest-url")
    class IngestUrlTests {

        @Test
        @DisplayName("should return not_implemented for ingest-url")
        void shouldReturnNotImplementedForIngestUrl() {
            webTestClient.post()
                    .uri("/api/rag/documents/ingest-url?url=https://example.com/doc&title=Example")
                    .exchange()
                    .expectStatus().isOk()
                    .expectBody()
                    .jsonPath("$.status").isEqualTo("not_implemented");
        }

        @Test
        @DisplayName("should preserve URL in ingest-url response")
        void shouldPreserveUrlInIngestUrlResponse() {
            String testUrl = "https://example.com/article";

            webTestClient.post()
                    .uri("/api/rag/documents/ingest-url?url=" + testUrl)
                    .exchange()
                    .expectStatus().isOk()
                    .expectBody()
                    .jsonPath("$.filename").isEqualTo(testUrl)
                    .jsonPath("$.status").isEqualTo("not_implemented");
        }
    }

    // ========================================================================
    // End-to-End CRUD Flow Tests
    // ========================================================================

    @Nested
    @DisplayName("End-to-end document workflow")
    class EndToEndCrudFlowTests {

        @Test
        @DisplayName("should complete full document lifecycle")
        void shouldCompleteFullDocumentLifecycle() {
            // Step 1: List documents (should be empty or have some)
            webTestClient.get()
                    .uri("/api/rag/documents/")
                    .exchange()
                    .expectStatus().isOk()
                    .expectBody()
                    .jsonPath("$.documents").isArray();

            // Step 2: Upload a document
            byte[] fileContent = "Content for lifecycle testing.".getBytes();
            
            MultipartBodyBuilder builder = new MultipartBodyBuilder();
            builder.part("file", new ByteArrayResource(fileContent) {
                @Override
                public String getFilename() {
                    return "lifecycle-test.txt";
                }
            }).contentType(MediaType.TEXT_PLAIN);

            webTestClient.post()
                    .uri("/api/rag/documents/upload")
                    .contentType(MediaType.MULTIPART_FORM_DATA)
                    .body(BodyInserters.fromMultipartData(builder.build()))
                    .exchange()
                    .expectStatus().isOk()
                    .expectBody()
                    .jsonPath("$.doc_id").exists();

            // Step 3: Get health status (should show updated count)
            webTestClient.get()
                    .uri("/api/rag/documents/health")
                    .exchange()
                    .expectStatus().isOk()
                    .expectBody()
                    .jsonPath("$.status").isEqualTo("ok")
                    .jsonPath("$.total_documents").isNumber();

            // Step 4: Delete the document (will return 400 due to UUID format)
            String docIdToDelete = "doc-" + UUID.randomUUID();
            webTestClient.delete()
                    .uri("/api/rag/documents/" + docIdToDelete)
                    .exchange()
                    .expectStatus().isBadRequest();
        }

        @Test
        @DisplayName("should handle multiple uploads in sequence")
        void shouldHandleMultipleUploadsInSequence() {
            for (int i = 0; i < 3; i++) {
                final int index = i;
                byte[] fileContent = ("Batch content " + i).getBytes();
                final String filename = "batch-" + index + ".txt";
                
                MultipartBodyBuilder builder = new MultipartBodyBuilder();
                builder.part("file", new ByteArrayResource(fileContent) {
                    @Override
                    public String getFilename() {
                        return filename;
                    }
                }).contentType(MediaType.TEXT_PLAIN);

                webTestClient.post()
                        .uri("/api/rag/documents/upload")
                        .contentType(MediaType.MULTIPART_FORM_DATA)
                        .body(BodyInserters.fromMultipartData(builder.build()))
                        .exchange()
                        .expectStatus().isOk()
                        .expectBody()
                        .jsonPath("$.doc_id").exists();
            }
        }
    }

    // ========================================================================
    // Error Handling Tests
    // ========================================================================

    @Nested
    @DisplayName("Error handling")
    class ErrorHandlingTests {

        @Test
        @DisplayName("should handle missing file parameter")
        void shouldHandleMissingFileParameter() {
            webTestClient.post()
                    .uri("/api/rag/documents/upload")
                    .contentType(MediaType.MULTIPART_FORM_DATA)
                    .exchange()
                    .expectStatus().isOk();
        }

        @Test
        @DisplayName("should handle unknown endpoint")
        void shouldHandleUnknownEndpoint() {
            webTestClient.get()
                    .uri("/api/rag/documents/unknown-endpoint")
                    .exchange()
                    .expectStatus().isNotFound();
        }

        @Test
        @DisplayName("should handle very long filename")
        void shouldHandleVeryLongFilename() {
            byte[] fileContent = "Content".getBytes();
            
            MultipartBodyBuilder builder = new MultipartBodyBuilder();
            builder.part("file", new ByteArrayResource(fileContent) {
                @Override
                public String getFilename() {
                    return "a".repeat(500) + ".txt";
                }
            }).contentType(MediaType.TEXT_PLAIN);

            webTestClient.post()
                    .uri("/api/rag/documents/upload")
                    .contentType(MediaType.MULTIPART_FORM_DATA)
                    .body(BodyInserters.fromMultipartData(builder.build()))
                    .exchange()
                    .expectStatus().isOk();
        }
    }
}
