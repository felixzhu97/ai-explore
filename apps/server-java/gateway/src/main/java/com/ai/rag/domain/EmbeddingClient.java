package com.ai.rag.domain;

/**
 * Repository interface for embedding operations.
 * Defined in Domain layer (ports), implemented in Infrastructure layer.
 */
public interface EmbeddingClient {

    /**
     * Generates an embedding for the given text.
     * Returns a float array representation of the embedding.
     */
    float[] embed(String text);

    /**
     * Generates embeddings for multiple texts.
     * Returns a list of float array embeddings.
     */
    float[][] embedAll(java.util.List<String> texts);

    /**
     * Gets the dimension of embeddings produced by this client.
     */
    int getDimension();
}
