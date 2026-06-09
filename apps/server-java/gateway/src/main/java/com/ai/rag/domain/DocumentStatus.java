package com.ai.rag.domain;

/**
 * Value object representing document processing status.
 * Part of the Document aggregate's state machine.
 */
public enum DocumentStatus {
    /**
     * Document has been created but not yet processed.
     */
    PENDING,

    /**
     * Document is currently being indexed/chunked.
     */
    INDEXING,

    /**
     * Document has been successfully processed and indexed.
     */
    COMPLETED,

    /**
     * Document processing failed.
     */
    FAILED;

    /**
     * Checks if the document can transition to a new state.
     */
    public boolean canTransitionTo(DocumentStatus newStatus) {
        return switch (this) {
            case PENDING -> newStatus == INDEXING || newStatus == FAILED;
            case INDEXING -> newStatus == COMPLETED || newStatus == FAILED;
            case COMPLETED, FAILED -> false;
        };
    }

    /**
     * Checks if the document is in a terminal state.
     */
    public boolean isTerminal() {
        return this == COMPLETED || this == FAILED;
    }
}
