package com.ai.agents.domain;

import java.util.List;
import java.util.Optional;

/**
 * Repository interface for Agent aggregate.
 * Port in the hexagonal architecture.
 */
public interface AgentRepository {

    /**
     * Save an agent.
     */
    void save(Agent agent);

    /**
     * Find an agent by its ID.
     */
    Optional<Agent> findById(AgentId id);

    /**
     * Find an agent by its type.
     */
    Optional<Agent> findByType(AgentType type);

    /**
     * Find all agents.
     */
    List<Agent> findAll();

    /**
     * Check if an agent exists by type.
     */
    boolean existsByType(AgentType type);

    /**
     * Delete an agent by ID.
     */
    void deleteById(AgentId id);

    /**
     * Get the count of agents.
     */
    int count();
}
