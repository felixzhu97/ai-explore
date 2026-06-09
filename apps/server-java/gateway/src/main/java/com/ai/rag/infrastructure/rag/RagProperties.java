package com.ai.rag.infrastructure.rag;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Configuration properties for RAG service.
 */
@ConfigurationProperties("rag")
public record RagProperties(
        Qdrant qdrant,
        Llm llm,
        Embedding embedding,
        Chunking chunking
) {

    public record Qdrant(
            String host,
            Integer port,
            String collectionName,
            Integer embeddingDimension,
            String apiKey
    ) {
        public String resolvedHost() { return host != null ? host : "localhost"; }
        public Integer resolvedPort() { return port != null ? port : 6333; }
        public String resolvedCollectionName() { return collectionName != null ? collectionName : "documents"; }
        public Integer resolvedEmbeddingDimension() { return embeddingDimension != null ? embeddingDimension : 384; }
    }

    public record Llm(
            String provider,
            String modelName,
            String apiKey,
            String baseUrl,
            Double temperature,
            Integer maxTokens,
            Integer timeoutSeconds
    ) {
        public String resolvedProvider() { return provider != null ? provider : "openai"; }
        public String resolvedModelName() { return modelName != null ? modelName : "gpt-4o"; }
        public Double resolvedTemperature() { return temperature != null ? temperature : 0.7; }
        public Integer resolvedMaxTokens() { return maxTokens != null ? maxTokens : 2048; }
        public Integer resolvedTimeoutSeconds() { return timeoutSeconds != null ? timeoutSeconds : 60; }
    }

    public record Embedding(
            String provider,
            String modelName,
            String apiKey,
            String baseUrl,
            Integer dimension
    ) {
        public String resolvedProvider() { return provider != null ? provider : "local"; }
        public String resolvedModelName() { return modelName != null ? modelName : "sentence-transformers/all-MiniLM-L6-v2"; }
        public Integer resolvedDimension() { return dimension != null ? dimension : 384; }
    }

    public record Chunking(
            Integer chunkSize,
            Integer chunkOverlap
    ) {
        public int resolvedChunkSize() { return chunkSize != null ? chunkSize : 500; }
        public int resolvedChunkOverlap() { return chunkOverlap != null ? chunkOverlap : 50; }
    }
}
