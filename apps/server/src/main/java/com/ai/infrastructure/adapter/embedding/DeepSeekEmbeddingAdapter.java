package com.ai.infrastructure.adapter.embedding;

import com.ai.application.port.EmbeddingPort;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.ai.embedding.EmbeddingRequest;
import org.springframework.ai.embedding.EmbeddingResponse;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

/**
 * DeepSeek embedding adapter implementing EmbeddingPort.
 * Uses Spring AI's OpenAI-compatible embedding model with DeepSeek API.
 */
@Component
public class DeepSeekEmbeddingAdapter implements EmbeddingPort {

    private static final Logger log = LoggerFactory.getLogger(DeepSeekEmbeddingAdapter.class);

    private final EmbeddingModel embeddingModel;

    public DeepSeekEmbeddingAdapter(EmbeddingModel embeddingModel) {
        this.embeddingModel = embeddingModel;
    }

    @Override
    public float[] embed(String text) {
        log.debug("Generating embedding for text (length={})", text.length());
        
        try {
            EmbeddingRequest request = new EmbeddingRequest(List.of(text), null);
            EmbeddingResponse response = embeddingModel.call(request);
            
            if (response.getResults() == null || response.getResults().isEmpty()) {
                throw new RuntimeException("Empty embedding response from DeepSeek API");
            }
            
            float[] result = response.getResults().get(0).getOutput();
            
            log.debug("Generated embedding with {} dimensions", result.length);
            return result;
            
        } catch (Exception e) {
            log.error("Failed to generate embedding for text", e);
            throw new RuntimeException("Embedding generation failed: " + e.getMessage(), e);
        }
    }

    @Override
    public List<float[]> embed(List<String> texts) {
        log.debug("Generating embeddings for {} texts", texts.size());
        
        try {
            EmbeddingRequest request = new EmbeddingRequest(texts, null);
            EmbeddingResponse response = embeddingModel.call(request);
            
            return response.getResults().stream()
                    .map(embeddingResult -> embeddingResult.getOutput())
                    .collect(Collectors.toList());
            
        } catch (Exception e) {
            log.error("Failed to generate embeddings for batch", e);
            throw new RuntimeException("Batch embedding generation failed: " + e.getMessage(), e);
        }
    }

    @Override
    public int getDimensions() {
        return 1536;
    }
}
