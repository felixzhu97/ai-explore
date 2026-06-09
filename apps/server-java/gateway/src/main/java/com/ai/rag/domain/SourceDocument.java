package com.ai.rag.domain;

/**
 * Value object representing a source document from vector search results.
 */
public record SourceDocument(
        String text,
        double score
) {

    public SourceDocument {
        if (text == null) {
            text = "";
        }
    }

    /**
     * Factory method.
     */
    public static SourceDocument of(String text, double score) {
        return new SourceDocument(text, score);
    }
}
