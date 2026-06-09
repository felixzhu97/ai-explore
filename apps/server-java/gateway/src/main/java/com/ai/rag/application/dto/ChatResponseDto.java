package com.ai.rag.application.dto;

import com.ai.rag.domain.SourceDocument;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

/**
 * Response DTO for chat operations.
 */
public record ChatResponseDto(
        String answer,
        @JsonProperty("session_id") String sessionId,
        List<SourceDocument> sources,
        String model,
        @JsonProperty("processing_time_ms") long processingTimeMs
) {

    /**
     * Factory method to create response with model and timing.
     */
    public static ChatResponseDto of(String answer, String sessionId, List<SourceDocument> sources, String model, long processingTimeMs) {
        return new ChatResponseDto(answer, sessionId, sources, model, processingTimeMs);
    }

    /**
     * Factory method without model/timing (for backward compatibility).
     */
    public static ChatResponseDto of(String answer, String sessionId, List<SourceDocument> sources) {
        return new ChatResponseDto(answer, sessionId, sources, "deepseek-v4-flash", 0);
    }
}
