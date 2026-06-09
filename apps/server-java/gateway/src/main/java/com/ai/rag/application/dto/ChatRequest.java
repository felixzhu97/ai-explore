package com.ai.rag.application.dto;

import jakarta.validation.constraints.NotBlank;

/**
 * Request DTO for chat operations.
 */
public record ChatRequest(
        @NotBlank(message = "Query is required") String query,
        String sessionId,
        Integer topK,
        Double temperature,
        String[] docIds
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
