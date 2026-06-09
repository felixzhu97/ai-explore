package com.ai.rag.infrastructure.embedding;

import com.ai.rag.domain.EmbeddingClient;

import java.util.List;

/**
 * OpenAI-compatible embedding client implementation.
 * Provides placeholder implementation for development.
 */
public class OpenAiEmbeddingClient implements EmbeddingClient {

    private final int dimension;

    public OpenAiEmbeddingClient() {
        this.dimension = 384; // Default for all-MiniLM-L6-v2
    }

    @Override
    public float[] embed(String text) {
        // Return a placeholder embedding
        return new float[dimension];
    }

    @Override
    public float[][] embedAll(List<String> texts) {
        // Return placeholder embeddings
        float[][] embeddings = new float[texts.size()][dimension];
        for (int i = 0; i < texts.size(); i++) {
            embeddings[i] = new float[dimension];
        }
        return embeddings;
    }

    @Override
    public int getDimension() {
        return dimension;
    }
}
