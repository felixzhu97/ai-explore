package com.ai.agents.infrastructure.adapter;

import com.ai.agents.domain.AgentType;
import com.ai.agents.domain.Conversation;
import com.ai.agents.presentation.dto.AgentRequestDto;
import com.ai.agents.presentation.dto.AgentResponseDto;
import reactor.core.publisher.Mono;

/**
 * Port interface for agent adapters.
 * Adapters implement this interface to provide agent capabilities.
 */
public interface AgentAdapter {

    /**
     * Get the agent type this adapter handles.
     */
    AgentType getType();

    /**
     * Execute the agent with the given conversation and request.
     */
    Mono<AgentResponseDto> execute(Conversation conversation, AgentRequestDto request);

    /**
     * Check if this adapter is available.
     */
    default boolean isAvailable() {
        return true;
    }
}
