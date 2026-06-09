package com.ai.rag.domain;

/**
 * Exception thrown when a document state transition is invalid.
 */
public class InvalidStateTransitionException extends DomainException {

    private final DocumentStatus currentStatus;
    private final DocumentStatus targetStatus;

    public InvalidStateTransitionException(DocumentStatus currentStatus, DocumentStatus targetStatus) {
        super(String.format("Invalid state transition from %s to %s", currentStatus, targetStatus));
        this.currentStatus = currentStatus;
        this.targetStatus = targetStatus;
    }

    public DocumentStatus getCurrentStatus() {
        return currentStatus;
    }

    public DocumentStatus getTargetStatus() {
        return targetStatus;
    }
}
