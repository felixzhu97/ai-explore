package com.ai.agents.presentation.dto;

import com.ai.agents.domain.AgentType;
import com.fasterxml.jackson.annotation.JsonInclude;

import java.util.List;
import java.util.Map;

/**
 * DTO for agent responses.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public record AgentResponseDto(
        String message,
        AgentType agentType,
        boolean streaming,
        String sessionId,
        List<String> sources,
        String error,
        Map<String, Object> metadata
) {
    public static AgentResponseDto success(String message, AgentType agentType) {
        return new AgentResponseDto(message, agentType, false, null, null, null, null);
    }

    public static AgentResponseDto success(String message, AgentType agentType, String sessionId) {
        return new AgentResponseDto(message, agentType, false, sessionId, null, null, null);
    }

    public static AgentResponseDto successWithSources(String message, AgentType agentType, List<String> sources) {
        return new AgentResponseDto(message, agentType, false, null, sources, null, null);
    }

    public static AgentResponseDto error(String errorMessage) {
        return new AgentResponseDto(null, null, false, null, null, errorMessage, null);
    }

    public static AgentResponseDto streaming(String message, AgentType agentType) {
        return new AgentResponseDto(message, agentType, true, null, null, null, null);
    }

    public AgentResponseDto withMetadata(Map<String, Object> newMetadata) {
        return new AgentResponseDto(message, agentType, streaming, sessionId, sources, error, newMetadata);
    }

    public AgentResponseDto withRoutedTo(AgentType routedTo) {
        return new AgentResponseDto(
                message,
                routedTo,
                streaming,
                sessionId,
                sources,
                error,
                Map.of("routedTo", routedTo.name())
        );
    }
}
