package com.ai.rag.domain;

/**
 * Value object representing a document title.
 * Immutable and validated on creation.
 */
public record DocumentTitle(String value) {

    public DocumentTitle {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("DocumentTitle cannot be blank");
        }
        value = value.trim();
    }

    /**
     * Factory method for creating a DocumentTitle.
     */
    public static DocumentTitle of(String value) {
        return new DocumentTitle(value);
    }

    @Override
    public String toString() {
        return value;
    }
}
