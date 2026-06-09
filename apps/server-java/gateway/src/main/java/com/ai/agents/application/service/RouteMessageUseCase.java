package com.ai.agents.application.service;

import com.ai.agents.application.dto.AgentRoutingResult;
import com.ai.agents.domain.RoutingDecision;
import com.ai.agents.domain.service.AgentRegistry;
import com.ai.agents.domain.service.SupervisorAgent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

/**
 * Use case for routing messages to appropriate agents.
 */
@Service
public class RouteMessageUseCase {

    private static final Logger log = LoggerFactory.getLogger(RouteMessageUseCase.class);

    private final SupervisorAgent supervisorAgent;
    private final AgentRegistry agentRegistry;

    public RouteMessageUseCase(
            SupervisorAgent supervisorAgent,
            AgentRegistry agentRegistry
    ) {
        this.supervisorAgent = supervisorAgent;
        this.agentRegistry = agentRegistry;
    }

    /**
     * Route a message to determine which agent should handle it.
     */
    public AgentRoutingResult route(String message) {
        log.debug("Routing message: {}", truncate(message, 50));

        RoutingDecision decision = supervisorAgent.route(message);

        log.info("Routing decision: target={}, confidence={}, reason={}",
                decision.targetType(), decision.confidence(), decision.reason());

        return AgentRoutingResult.from(decision);
    }

    /**
     * Route a message with explicit agent type preference.
     */
    public AgentRoutingResult routeWithPreference(String message, com.ai.agents.domain.AgentType preferredType) {
        log.debug("Routing message with preference: {} -> {}", truncate(message, 50), preferredType);

        RoutingDecision decision = supervisorAgent.routeTo(preferredType, message);

        log.info("Routing decision with preference: target={}, confidence={}, reason={}",
                decision.targetType(), decision.confidence(), decision.reason());

        return AgentRoutingResult.from(decision);
    }

    /**
     * Check if an agent type is available.
     */
    public boolean isAgentAvailable(com.ai.agents.domain.AgentType type) {
        return agentRegistry.hasAgent(type);
    }

    private String truncate(String text, int maxLength) {
        if (text == null) return "";
        return text.length() > maxLength ? text.substring(0, maxLength) + "..." : text;
    }
}
