package com.ai.agents.presentation.controller;

import com.ai.agents.application.service.AgentOrchestrationService;
import com.ai.agents.domain.AgentType;
import com.ai.agents.domain.service.AgentRegistry;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Health check controller for AI agents.
 */
@RestController
@RequestMapping("/api/agents")
public class AgentHealthController {

    private final AgentOrchestrationService orchestrationService;
    private final AgentRegistry agentRegistry;

    public AgentHealthController(
            AgentOrchestrationService orchestrationService,
            AgentRegistry agentRegistry
    ) {
        this.orchestrationService = orchestrationService;
        this.agentRegistry = agentRegistry;
    }

    /**
     * Overall health check.
     */
    @GetMapping("/health")
    public ResponseEntity<Map<String, Object>> health() {
        Map<String, Object> health = new LinkedHashMap<>();
        health.put("status", "UP");
        health.put("timestamp", Instant.now().toString());
        health.put("service", "gateway-agents");

        // Agent status
        Map<String, String> agentStatuses = new LinkedHashMap<>();
        List<AgentType> availableTypes = orchestrationService.getAvailableTypes();

        for (AgentType type : AgentType.values()) {
            if (type == AgentType.SUPERVISOR) {
                continue;
            }
            String status = availableTypes.contains(type) ? "UP" : "DOWN";
            agentStatuses.put(type.getId(), status);
        }
        health.put("agents", agentStatuses);
        health.put("registeredAgents", agentRegistry.size());

        return ResponseEntity.ok(health);
    }

    /**
     * Detailed agent information.
     */
    @GetMapping("/status")
    public ResponseEntity<Map<String, Object>> status() {
        Map<String, Object> status = new LinkedHashMap<>();
        status.put("timestamp", Instant.now().toString());

        List<Map<String, Object>> agentDetails = orchestrationService.listAgents().stream()
                .map(info -> Map.<String, Object>of(
                        "id", info.id(),
                        "name", info.name(),
                        "description", info.description(),
                        "status", info.status()
                ))
                .toList();

        status.put("agents", agentDetails);
        status.put("totalCount", agentDetails.size());

        return ResponseEntity.ok(status);
    }
}
