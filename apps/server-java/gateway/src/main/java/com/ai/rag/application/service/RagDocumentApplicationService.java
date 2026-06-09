package com.ai.rag.application.service;

import com.ai.rag.application.dto.DocumentDTO;
import com.ai.rag.domain.*;
import com.ai.rag.domain.service.ChunkingService;
import com.ai.rag.infrastructure.exception.RagException;
import com.ai.rag.infrastructure.rag.RagProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.*;

/**
 * Application service for document operations.
 * Thin orchestration layer - delegates to domain objects.
 */
@Service
public class RagDocumentApplicationService {

    private static final Logger log = LoggerFactory.getLogger(RagDocumentApplicationService.class);

    private final DocumentRepository documentRepository;
    private final VectorStore vectorStore;
    private final ChunkingService chunkingService;
    private final RagProperties properties;

    public RagDocumentApplicationService(
            DocumentRepository documentRepository,
            VectorStore vectorStore,
            ChunkingService chunkingService,
            RagProperties properties
    ) {
        this.documentRepository = documentRepository;
        this.vectorStore = vectorStore;
        this.chunkingService = chunkingService;
        this.properties = properties;
    }

    /**
     * Uploads and ingests a document.
     */
    public DocumentDTO upload(MultipartFile file) {
        String filename = file.getOriginalFilename();
        if (filename == null || filename.isBlank()) {
            throw RagException.validation("Filename must not be blank");
        }

        String contentType = file.getContentType();
        Long size = file.getSize();

        String content;
        try {
            content = new String(file.getBytes(), StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw RagException.processing("Failed to read uploaded file", e);
        }

        return ingestContent(filename, contentType, size, content);
    }

    /**
     * Ingests raw text content as a document.
     */
    public DocumentDTO ingestText(String title, String content) {
        return ingestContent(title, "text/plain", (long) content.length(), content);
    }

    /**
     * Internal method to ingest content.
     */
    private DocumentDTO ingestContent(String filename, String contentType, Long size, String content) {
        // Create domain object
        Document doc = Document.create(
                DocumentTitle.of(filename),
                contentType,
                size
        );

        // Save initial state
        documentRepository.save(doc);
        doc.startProcessing();

        try {
            // Chunk the content using domain service
            ChunkingPolicy policy = ChunkingPolicy.bySize(
                    properties.chunking().resolvedChunkSize(),
                    properties.chunking().resolvedChunkOverlap()
            );
            List<Chunk> chunks = chunkingService.chunk(DocumentContent.of(content), policy, doc.getId());

            if (chunks.isEmpty()) {
                doc.fail("No content extracted from document");
                throw RagException.validation("No content extracted from document");
            }

            // Add chunks to domain object
            doc.addChunks(chunks);

            // Add to vector store
            List<String> chunkTexts = chunks.stream().map(Chunk::text).toList();
            vectorStore.addSegments(chunkTexts, doc.getId(), filename);

            // Complete processing
            doc.completeProcessing();
            documentRepository.update(doc);

            log.info("Document ingested: {} with {} chunks", doc.getId(), chunks.size());
            return DocumentDTO.fromDomain(doc);

        } catch (Exception e) {
            log.error("Failed to ingest document: {}", filename, e);
            doc.fail(e.getMessage());
            documentRepository.update(doc);
            throw RagException.processing("Failed to ingest document: " + e.getMessage(), e);
        }
    }

    /**
     * Lists all documents with pagination.
     */
    public List<DocumentDTO> findAll(int page, int size) {
        return documentRepository.findAll(page, size).stream()
                .map(DocumentDTO::fromDomain)
                .toList();
    }

    /**
     * Finds a document by ID.
     */
    public Optional<DocumentDTO> findById(String id) {
        try {
            return documentRepository.findById(DocumentId.of(id))
                    .map(DocumentDTO::fromDomain);
        } catch (IllegalArgumentException e) {
            return Optional.empty();
        }
    }

    /**
     * Deletes a document.
     */
    public void delete(String id) {
        DocumentId docId;
        try {
            docId = DocumentId.of(id);
        } catch (IllegalArgumentException e) {
            throw RagException.validation("Invalid document ID format");
        }

        // Delete from vector store
        vectorStore.deleteByDocId(docId);

        // Delete from repository
        documentRepository.deleteById(docId);

        log.info("Deleted document: {}", id);
    }

    /**
     * Gets document statistics.
     */
    public Map<String, Object> getStats(String id) {
        DocumentDTO doc = findById(id)
                .orElseThrow(() -> RagException.notFound("Document not found: " + id));

        Map<String, Object> stats = new HashMap<>();
        stats.put("doc_id", doc.id());
        stats.put("filename", doc.title());
        stats.put("content_type", doc.contentType());
        stats.put("size", doc.size());
        stats.put("chunk_count", doc.chunkCount());
        stats.put("status", doc.status());
        stats.put("created_at", doc.createdAt().toString());
        stats.put("vector_stats", vectorStore.getStats());

        return stats;
    }

    /**
     * Returns all document IDs.
     */
    public Set<String> findAllIds() {
        return documentRepository.findAllIds().stream()
                .map(DocumentId::toString)
                .collect(java.util.stream.Collectors.toSet());
    }

    /**
     * Returns total document count.
     */
    public long count() {
        return documentRepository.count();
    }
}
