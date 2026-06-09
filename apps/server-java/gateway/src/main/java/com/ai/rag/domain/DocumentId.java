package com.ai.rag.domain;

import java.util.UUID;

/**
 * Value object representing a unique document identifier.
 * Immutable and validated on creation.
 */
public record DocumentId(UUID value) {

    public DocumentId {
        if (value == null) {
            throw new IllegalArgumentException("DocumentId cannot be null");
        }
    }

    /**
     * Factory method to create a new random DocumentId.
     */
    public static DocumentId generate() {
        return new DocumentId(UUID.randomUUID());
    }

    /**
     * Factory method to create a DocumentId from a string.
     */
    public static DocumentId of(String value) {
        return new DocumentId(UUID.fromString(value));
    }

    @Override
    public String toString() {
        return value.toString();
    }
}
