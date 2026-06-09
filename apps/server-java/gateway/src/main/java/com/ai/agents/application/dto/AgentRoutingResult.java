package com.ai.agents.application.dto;

import com.ai.agents.domain.AgentType;
import com.ai.agents.domain.RoutingDecision;

/**
 * Result of agent routing operation.
 */
public record AgentRoutingResult(
        AgentType targetType,
        double confidence,
        String reason
) {
    public static AgentRoutingResult from(RoutingDecision decision) {
        return new AgentRoutingResult(
                decision.targetType(),
                decision.confidence(),
                decision.reason()
        );
    }

    public static AgentRoutingResult fallback() {
        return new AgentRoutingResult(AgentType.CHAT, 0.5, "Fallback to chat");
    }
}
