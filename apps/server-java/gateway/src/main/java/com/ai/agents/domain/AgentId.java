package com.ai.agents.domain;

/**
 * Agent identifier value object.
 * Encapsulates the unique identifier for an agent.
 */
public final class AgentId {
    private final String value;

    private AgentId(String value) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("AgentId cannot be null or blank");
        }
        this.value = value;
    }

    public static AgentId of(String value) {
        return new AgentId(value);
    }

    public String value() {
        return value;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        AgentId agentId = (AgentId) o;
        return value.equals(agentId.value);
    }

    @Override
    public int hashCode() {
        return value.hashCode();
    }

    @Override
    public String toString() {
        return value;
    }
}
