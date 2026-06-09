package com.ai.rag.domain;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Value object representing a text chunk from a document.
 * Immutable and identified by its position within the document.
 */
public record Chunk(
        String text,
        int position,
        @JsonProperty("source_document_id") DocumentId sourceDocumentId
) {

    public Chunk {
        if (text == null || text.isBlank()) {
            throw new IllegalArgumentException("Chunk text cannot be blank");
        }
        if (position < 0) {
            throw new IllegalArgumentException("Chunk position cannot be negative");
        }
        if (sourceDocumentId == null) {
            throw new IllegalArgumentException("Chunk must have a source document ID");
        }
    }

    /**
     * Factory method to create a Chunk.
     */
    public static Chunk create(String text, int position, DocumentId sourceDocumentId) {
        return new Chunk(text, position, sourceDocumentId);
    }

    /**
     * Returns the length of the chunk text.
     */
    public int length() {
        return text.length();
    }

    @Override
    public String toString() {
        return "Chunk{position=" + position + ", length=" + text.length() + "}";
    }
}
