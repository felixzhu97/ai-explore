package com.ai.interfaces.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * RAG chat request DTO.
 */
public record RagChatRequest(
    @JsonProperty("query")
    String question,

    String sessionId
) {}
