package com.ai.rag.application.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;

/**
 * Request DTO for chat operations.
 */
public record ChatRequest(
        @NotBlank(message = "Query is required") String query,
        @JsonProperty("session_id") String sessionId,
        @JsonProperty("top_k") Integer topK,
        Double temperature,
        @JsonProperty("doc_ids") String[] docIds
) {

    /**
     * Returns the effective topK value.
     */
    public int effectiveTopK() {
        return topK != null && topK > 0 ? topK : 5;
    }

    /**
     * Returns the effective temperature value.
     */
    public double effectiveTemperature() {
        return temperature != null ? temperature : 0.7;
    }
}
