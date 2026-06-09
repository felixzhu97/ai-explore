package com.ai.agents.domain;

/**
 * Agent name value object.
 * Encapsulates the display name for an agent with validation.
 */
public final class AgentName {
    private final String value;

    private AgentName(String value) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("AgentName cannot be null or blank");
        }
        if (value.length() > 100) {
            throw new IllegalArgumentException("AgentName cannot exceed 100 characters");
        }
        this.value = value.trim();
    }

    public static AgentName of(String value) {
        return new AgentName(value);
    }

    public String value() {
        return value;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        AgentName agentName = (AgentName) o;
        return value.equals(agentName.value);
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
