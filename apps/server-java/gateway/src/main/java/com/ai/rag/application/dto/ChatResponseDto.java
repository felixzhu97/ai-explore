package com.ai.rag.application.dto;

import com.ai.rag.domain.SourceDocument;

import java.util.List;

/**
 * Response DTO for chat operations.
 */
public record ChatResponseDto(
        String answer,
        String sessionId,
        List<SourceDocument> sources
) {

    /**
     * Factory method to create response.
     */
    public static ChatResponseDto of(String answer, String sessionId, List<SourceDocument> sources) {
        return new ChatResponseDto(answer, sessionId, sources);
    }
}
