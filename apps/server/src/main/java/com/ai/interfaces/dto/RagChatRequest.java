package com.ai.interfaces.dto;

import jakarta.validation.constraints.NotBlank;

/**
 * RAG chat request DTO.
 */
public record RagChatRequest(
    @NotBlank(message = "Question is required")
    String question,

    String sessionId
) {}
