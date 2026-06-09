package com.ai.agents.domain.service;

import com.ai.agents.domain.Agent;
import com.ai.agents.domain.AgentCapabilities;
import com.ai.agents.domain.AgentId;
import com.ai.agents.domain.AgentName;
import com.ai.agents.domain.AgentType;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Domain service for agent registration and discovery.
 * Manages the registry of available agents in the system.
 * 
 * This is a pure domain service with no framework dependencies.
 */
public final class AgentRegistry {

    private final Map<AgentType, Agent> agentsByType;
    private final Map<AgentId, Agent> agentsById;

    public AgentRegistry() {
        this.agentsByType = new ConcurrentHashMap<>();
        this.agentsById = new ConcurrentHashMap<>();
    }

    /**
     * Create registry with initial agents.
     */
    public AgentRegistry(List<Agent> initialAgents) {
        this();
        if (initialAgents != null) {
            initialAgents.forEach(this::register);
        }
    }

    /**
     * Register an agent.
     */
    public void register(Agent agent) {
        if (agent == null) {
            throw new IllegalArgumentException("Agent cannot be null");
        }
        agentsByType.put(agent.type(), agent);
        agentsById.put(agent.id(), agent);
    }

    /**
     * Register an agent by parameters.
     */
    public Agent register(String id, String name, AgentType type) {
        Agent agent = Agent.create(
                AgentId.of(id),
                AgentName.of(name),
                type,
                AgentCapabilities.of(type)
        );
        register(agent);
        return agent;
    }

    /**
     * Unregister an agent by ID.
     */
    public void unregister(AgentId id) {
        Agent agent = agentsById.remove(id);
        if (agent != null) {
            agentsByType.remove(agent.type());
        }
    }

    /**
     * Find agent by type.
     */
    public Optional<Agent> findByType(AgentType type) {
        return Optional.ofNullable(agentsByType.get(type));
    }

    /**
     * Find agent by ID.
     */
    public Optional<Agent> findById(AgentId id) {
        return Optional.ofNullable(agentsById.get(id));
    }

    /**
     * Get all registered agents.
     */
    public List<Agent> getAllAgents() {
        return new ArrayList<>(agentsById.values());
    }

    /**
     * Get all agent types.
     */
    public Set<AgentType> getAvailableTypes() {
        return Collections.unmodifiableSet(agentsByType.keySet());
    }

    /**
     * Check if agent type is available.
     */
    public boolean hasAgent(AgentType type) {
        return agentsByType.containsKey(type);
    }

    /**
     * Get agent count.
     */
    public int size() {
        return agentsById.size();
    }

    /**
     * Check if registry is empty.
     */
    public boolean isEmpty() {
        return agentsById.isEmpty();
    }

    /**
     * Clear all agents.
     */
    public void clear() {
        agentsByType.clear();
        agentsById.clear();
    }

    /**
     * Create a default registry with standard agents.
     */
    public static AgentRegistry createDefault() {
        AgentRegistry registry = new AgentRegistry();
        registry.register("chat", "ChatAgent", AgentType.CHAT);
        registry.register("rag", "RagAgent", AgentType.RAG);
        registry.register("tts", "TtsAgent", AgentType.TTS);
        registry.register("vision", "VisionAgent", AgentType.VISION);
        registry.register("media", "MediaAgent", AgentType.MEDIA);
        registry.register("text", "TextAgent", AgentType.TEXT);
        registry.register("supervisor", "SupervisorAgent", AgentType.SUPERVISOR);
        return registry;
    }
}
