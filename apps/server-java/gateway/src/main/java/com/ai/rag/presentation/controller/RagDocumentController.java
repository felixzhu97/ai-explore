package com.ai.rag.presentation.dto;

import com.ai.rag.application.dto.DocumentDTO;
import com.ai.rag.application.service.RagDocumentApplicationService;
import com.ai.rag.infrastructure.exception.RagException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * REST controller for document management operations.
 * Handles DTO conversion and delegates to Application Service.
 */
@RestController
@RequestMapping("/api/rag/documents")
public class RagDocumentController {

    private static final Logger log = LoggerFactory.getLogger(RagDocumentController.class);

    private final RagDocumentApplicationService documentService;

    public RagDocumentController(RagDocumentApplicationService documentService) {
        this.documentService = documentService;
    }

    /**
     * List all documents.
     * GET /api/rag/documents/
     */
    @GetMapping("/")
    public Mono<DocumentListResponse> list(
            @RequestParam(defaultValue = "0") int skip,
            @RequestParam(defaultValue = "100") int limit
    ) {
        log.info("List documents: skip={}, limit={}", skip, limit);

        return Mono.fromCallable(() -> {
            List<DocumentDTO> docs = documentService.findAll(skip, limit);
            return new DocumentListResponse(
                    docs.stream()
                            .map(d -> new DocumentListResponse.DocItem(
                                    d.id(),
                                    d.title(),
                                    d.chunkCount(),
                                    d.contentType(),
                                    d.size(),
                                    d.createdAt().toString()
                            ))
                            .collect(Collectors.toList()),
                    docs.size()
            );
        });
    }

    /**
     * Upload and ingest a document.
     * POST /api/rag/documents/upload
     */
    @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public Mono<ResponseEntity<UploadResponse>> upload(
            @RequestParam("file") MultipartFile file,
            @RequestParam(value = "title", required = false) String title
    ) {
        log.info("Upload request: filename='{}', size={}", file.getOriginalFilename(), file.getSize());

        return Mono.fromCallable(() -> {
            try {
                DocumentDTO doc = documentService.upload(file);
                return ResponseEntity.ok(new UploadResponse(
                        doc.id(),
                        doc.title(),
                        doc.chunkCount(),
                        "success"
                ));
            } catch (RagException e) {
                log.error("Upload failed: {}", e.getMessage());
                return ResponseEntity.badRequest().body(new UploadResponse(
                        null,
                        file.getOriginalFilename(),
                        0,
                        "failed: " + e.getMessage()
                ));
            }
        });
    }

    /**
     * Ingest a document from URL.
     * POST /api/rag/documents/ingest-url
     */
    @PostMapping(value = "/ingest-url")
    public Mono<UploadResponse> ingestUrl(
            @RequestParam("url") String url,
            @RequestParam(value = "title", required = false) String title
    ) {
        log.info("Ingest URL: url='{}', title='{}'", url, title);

        // TODO: Implement URL content fetching
        return Mono.just(new UploadResponse(
                null,
                url,
                0,
                "not_implemented"
        ));
    }

    /**
     * Get document details.
     * GET /api/rag/documents/{doc_id}
     */
    @GetMapping("/{docId}")
    public Mono<ResponseEntity<DocumentDTO>> get(@PathVariable String docId) {
        log.info("Get document: {}", docId);

        return Mono.fromCallable(() -> {
            return documentService.findById(docId)
                    .map(ResponseEntity::ok)
                    .orElse(ResponseEntity.notFound().<DocumentDTO>build());
        });
    }

    /**
     * Get document statistics.
     * GET /api/rag/documents/{doc_id}/stats
     */
    @GetMapping("/{docId}/stats")
    public Mono<ResponseEntity<Map<String, Object>>> getStats(@PathVariable String docId) {
        log.info("Get document stats: {}", docId);

        return Mono.fromCallable(() -> {
            try {
                Map<String, Object> stats = documentService.getStats(docId);
                return ResponseEntity.ok(stats);
            } catch (RagException e) {
                return ResponseEntity.notFound().build();
            }
        });
    }

    /**
     * Delete a document.
     * DELETE /api/rag/documents/{doc_id}
     */
    @DeleteMapping("/{docId}")
    public Mono<ResponseEntity<Map<String, String>>> delete(@PathVariable String docId) {
        log.info("Delete document: {}", docId);

        return Mono.fromCallable(() -> {
            try {
                documentService.delete(docId);
                return ResponseEntity.ok(Map.of(
                        "status", "success",
                        "message", "Document " + docId + " deleted"
                ));
            } catch (RagException e) {
                return ResponseEntity.badRequest().body(Map.of(
                        "status", "error",
                        "message", e.getMessage()
                ));
            }
        });
    }

    /**
     * Re-index a document.
     * POST /api/rag/documents/{doc_id}/reindex
     */
    @PostMapping("/{docId}/reindex")
    public Mono<ResponseEntity<Map<String, String>>> reindex(@PathVariable String docId) {
        log.info("Reindex document: {}", docId);

        // TODO: Implement re-indexing
        return Mono.just(ResponseEntity.ok(Map.of(
                "status", "info",
                "message", "Please re-upload the document to re-index"
        )));
    }

    /**
     * Clean up all documents.
     * POST /api/rag/documents/cleanup-all
     */
    @PostMapping("/cleanup-all")
    public Mono<ResponseEntity<Map<String, Object>>> cleanupAll() {
        log.warn("Cleanup all documents requested");

        return Mono.just(ResponseEntity.ok(Map.of(
                "status", "info",
                "message", "Cleanup not implemented"
        )));
    }

    /**
     * Health check endpoint.
     * GET /api/rag/documents/health
     */
    @GetMapping("/health")
    public Mono<Map<String, Object>> health() {
        return Mono.fromCallable(() -> Map.of(
                "status", "ok",
                "total_documents", documentService.count(),
                "vector_stats", documentService.findAllIds()
        ));
    }

    // Response records

    public record DocumentListResponse(
            List<DocItem> documents,
            int total
    ) {
        public record DocItem(
                String doc_id,
                String filename,
                int chunk_count,
                String content_type,
                Long size,
                String created_at
        ) {}
    }

    public record UploadResponse(
            String doc_id,
            String filename,
            int chunks,
            String status
    ) {}
}
