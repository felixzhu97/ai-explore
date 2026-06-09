package com.ai.agents.presentation.dto;

import com.ai.agents.domain.AgentType;
import com.fasterxml.jackson.annotation.JsonInclude;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.Map;

/**
 * DTO for agent requests.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public record AgentRequestDto(
        @NotBlank(message = "User message is required")
        String message,

        @NotNull(message = "Agent type is required")
        AgentType agentType,

        String sessionId,

        Integer topK,

        String model,

        Map<String, Object> metadata
) {
    public AgentRequestDto {
        if (metadata == null) {
            metadata = Map.of();
        }
    }

    public static AgentRequestDto of(String message, AgentType agentType) {
        return new AgentRequestDto(message, agentType, null, null, null, null);
    }

    public static AgentRequestDto of(String message, AgentType agentType, String sessionId) {
        return new AgentRequestDto(message, agentType, sessionId, null, null, null);
    }
}
