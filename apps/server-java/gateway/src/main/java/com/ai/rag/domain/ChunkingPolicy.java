package com.ai.rag.domain;

/**
 * Value object representing the chunking policy for document processing.
 * Immutable configuration for text chunking.
 */
public record ChunkingPolicy(
        int chunkSize,
        int overlap,
        ChunkingStrategy strategy
) {

    public ChunkingPolicy {
        if (chunkSize <= 0) {
            throw new IllegalArgumentException("Chunk size must be positive");
        }
        if (overlap < 0) {
            throw new IllegalArgumentException("Overlap cannot be negative");
        }
        if (overlap >= chunkSize) {
            throw new IllegalArgumentException("Overlap must be smaller than chunk size");
        }
        if (strategy == null) {
            throw new IllegalArgumentException("Chunking strategy cannot be null");
        }
    }

    /**
     * Default chunking policy.
     */
    public static ChunkingPolicy defaultPolicy() {
        return new ChunkingPolicy(500, 50, ChunkingStrategy.BY_SIZE);
    }

    /**
     * Create a simple size-based chunking policy.
     */
    public static ChunkingPolicy bySize(int chunkSize, int overlap) {
        return new ChunkingPolicy(chunkSize, overlap, ChunkingStrategy.BY_SIZE);
    }

    /**
     * Create a paragraph-aware chunking policy.
     */
    public static ChunkingPolicy byParagraphs(int chunkSize, int overlap) {
        return new ChunkingPolicy(chunkSize, overlap, ChunkingStrategy.BY_PARAGRAPH);
    }

    /**
     * Chunking strategies.
     */
    public enum ChunkingStrategy {
        /**
         * Split text by size with overlap.
         */
        BY_SIZE,

        /**
         * Split text by paragraphs first, then by size.
         */
        BY_PARAGRAPH
    }
}
