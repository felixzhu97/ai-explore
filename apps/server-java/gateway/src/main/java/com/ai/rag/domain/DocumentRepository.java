package com.ai.rag.domain;

import java.util.List;
import java.util.Optional;
import java.util.Set;

/**
 * Repository interface for Document aggregate.
 * Defined in Domain layer (ports), implemented in Infrastructure layer.
 */
public interface DocumentRepository {

    /**
     * Saves a new document.
     */
    Document save(Document document);

    /**
     * Updates an existing document.
     */
    void update(Document document);

    /**
     * Finds a document by its ID.
     */
    Optional<Document> findById(DocumentId id);

    /**
     * Finds all documents with pagination.
     */
    List<Document> findAll(int page, int size);

    /**
     * Finds all documents with a specific status.
     */
    List<Document> findByStatus(DocumentStatus status, int page, int size);

    /**
     * Deletes a document by its ID.
     */
    void deleteById(DocumentId id);

    /**
     * Counts all documents.
     */
    long count();

    /**
     * Counts documents by status.
     */
    long countByStatus(DocumentStatus status);

    /**
     * Returns all document IDs.
     */
    Set<DocumentId> findAllIds();

    /**
     * Checks if a document exists.
     */
    boolean exists(DocumentId id);
}
