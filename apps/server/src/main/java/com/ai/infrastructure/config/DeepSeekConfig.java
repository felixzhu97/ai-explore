package com.ai.infrastructure.config;

import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.ai.openai.OpenAiEmbeddingModel;
import org.springframework.ai.openai.api.OpenAiApi;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.lang.NonNull;

/**
 * Configuration for DeepSeek embedding service.
 * Conditionally creates either a real OpenAI-compatible client or a mock implementation.
 */
@Configuration
public class DeepSeekConfig {

    @Value("${spring.ai.openai.base-url:https://api.deepseek.com}")
    private String baseUrl;

    @Value("${spring.ai.openai.api-key:}")
    private String apiKey;

    @Bean
    @ConditionalOnProperty(name = "rag.mock.embeddings", havingValue = "false", matchIfMissing = true)
    @NonNull
    public EmbeddingModel embeddingModel() {
        if (apiKey == null || apiKey.isBlank() || apiKey.equals("your-api-key")) {
            throw new IllegalStateException(
                "DeepSeek API key not configured. Set DEEPSEEK_API_KEY environment variable " +
                "or enable mock embeddings with rag.mock.embeddings=true"
            );
        }
        OpenAiApi api = OpenAiApi.builder()
                .baseUrl(baseUrl)
                .apiKey(apiKey)
                .build();
        return new OpenAiEmbeddingModel(api);
    }
}
