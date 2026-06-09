package com.ai.rag.domain;

/**
 * Value object representing document content.
 * Immutable and validated on creation.
 */
public record DocumentContent(String value) {

    public DocumentContent {
        if (value == null) {
            throw new IllegalArgumentException("DocumentContent cannot be null");
        }
    }

    /**
     * Factory method for creating DocumentContent.
     */
    public static DocumentContent of(String value) {
        return new DocumentContent(value);
    }

    /**
     * Returns the length of the content.
     */
    public int length() {
        return value.length();
    }

    /**
     * Checks if the content is empty.
     */
    public boolean isEmpty() {
        return value.isEmpty();
    }

    /**
     * Checks if the content is blank (empty or whitespace only).
     */
    public boolean isBlank() {
        return value.isBlank();
    }

    @Override
    public String toString() {
        return value;
    }
}
