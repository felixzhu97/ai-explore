package com.ai.rag.domain;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

/**
 * Aggregate root for Document entity.
 * Rich domain model with business logic and state management.
 * 
 * Invariants:
 * - Document can only be processed once (from PENDING state)
 * - Status transitions follow defined state machine rules
 * - Chunks are immutable once added
 */
public class Document {

    private final DocumentId id;
    private final DocumentTitle title;
    private final String contentType;
    private final Long size;
    private final LocalDateTime createdAt;
    private final LocalDateTime updatedAt;
    
    private DocumentStatus status;
    private List<Chunk> chunks;

    // Private constructor - use factory methods
    private Document(
            DocumentId id,
            DocumentTitle title,
            String contentType,
            Long size,
            DocumentStatus status,
            LocalDateTime createdAt,
            LocalDateTime updatedAt,
            List<Chunk> chunks
    ) {
        this.id = id;
        this.title = title;
        this.contentType = contentType;
        this.size = size;
        this.status = status;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
        this.chunks = new ArrayList<>(chunks);
    }

    /**
     * Factory method to create a new Document ready for processing.
     */
    public static Document create(DocumentTitle title, String contentType, Long size) {
        LocalDateTime now = LocalDateTime.now();
        return new Document(
                DocumentId.generate(),
                title,
                contentType,
                size,
                DocumentStatus.PENDING,
                now,
                now,
                List.of()
        );
    }

    /**
     * Factory method to reconstitute a Document from persistence.
     */
    public static Document reconstitute(
            DocumentId id,
            DocumentTitle title,
            String contentType,
            Long size,
            DocumentStatus status,
            LocalDateTime createdAt,
            LocalDateTime updatedAt,
            List<Chunk> chunks
    ) {
        return new Document(id, title, contentType, size, status, createdAt, updatedAt, chunks);
    }

    // Getters
    public DocumentId getId() {
        return id;
    }

    public DocumentTitle getTitle() {
        return title;
    }

    public String getContentType() {
        return contentType;
    }

    public Long getSize() {
        return size;
    }

    public DocumentStatus getStatus() {
        return status;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    /**
     * Returns an unmodifiable view of chunks.
     */
    public List<Chunk> getChunks() {
        return Collections.unmodifiableList(chunks);
    }

    public int getChunkCount() {
        return chunks.size();
    }

    // Business methods

    /**
     * Checks if this document can be processed.
     */
    public boolean canBeProcessed() {
        return status == DocumentStatus.PENDING;
    }

    /**
     * Starts processing the document.
     * Transitions from PENDING to INDEXING state.
     * 
     * @throws InvalidStateTransitionException if document is not in PENDING state
     */
    public void startProcessing() {
        if (status != DocumentStatus.PENDING) {
            throw new InvalidStateTransitionException(status, DocumentStatus.INDEXING);
        }
        this.status = DocumentStatus.INDEXING;
    }

    /**
     * Adds chunks to the document during processing.
     * Can only be called when document is in INDEXING state.
     * 
     * @param newChunks Chunks to add
     * @throws DomainException if document is not being processed
     */
    public void addChunks(List<Chunk> newChunks) {
        if (status != DocumentStatus.INDEXING) {
            throw new DomainException("Cannot add chunks to document that is not being processed");
        }
        this.chunks.addAll(newChunks);
    }

    /**
     * Completes processing successfully.
     * Transitions from INDEXING to COMPLETED state.
     * 
     * @throws InvalidStateTransitionException if document is not in INDEXING state
     * @throws DomainException if no chunks were created
     */
    public void completeProcessing() {
        if (status != DocumentStatus.INDEXING) {
            throw new InvalidStateTransitionException(status, DocumentStatus.COMPLETED);
        }
        if (chunks.isEmpty()) {
            throw new DomainException("Cannot complete processing without any chunks");
        }
        this.status = DocumentStatus.COMPLETED;
    }

    /**
     * Marks processing as failed.
     * Transitions from any non-terminal state to FAILED.
     * 
     * @param reason Reason for failure
     */
    public void fail(String reason) {
        if (status.isTerminal()) {
            throw new DomainException("Cannot fail a document that is already in terminal state: " + status);
        }
        this.status = DocumentStatus.FAILED;
    }

    /**
     * Checks if the document has been successfully processed.
     */
    public boolean isCompleted() {
        return status == DocumentStatus.COMPLETED;
    }

    /**
     * Checks if the document processing failed.
     */
    public boolean hasFailed() {
        return status == DocumentStatus.FAILED;
    }

    @Override
    public String toString() {
        return "Document{" +
                "id=" + id +
                ", title=" + title +
                ", status=" + status +
                ", chunkCount=" + chunks.size() +
                '}';
    }
}
