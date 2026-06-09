package com.ai.agents.domain;

import java.time.Instant;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

/**
 * Agent session aggregate root.
 * Manages a conversation session with a specific agent.
 */
public final class AgentSession {
    private final SessionId id;
    private final AgentId agentId;
    private final Instant createdAt;
    private final Instant lastAccessedAt;
    private final Map<String, Object> sessionData;
    private final SessionStatus status;

    private AgentSession(
            SessionId id,
            AgentId agentId,
            Instant createdAt,
            Instant lastAccessedAt,
            Map<String, Object> sessionData,
            SessionStatus status
    ) {
        this.id = Objects.requireNonNull(id, "SessionId cannot be null");
        this.agentId = Objects.requireNonNull(agentId, "AgentId cannot be null");
        this.createdAt = Objects.requireNonNull(createdAt, "CreatedAt cannot be null");
        this.lastAccessedAt = Objects.requireNonNull(lastAccessedAt, "LastAccessedAt cannot be null");
        this.sessionData = sessionData != null ? Map.copyOf(sessionData) : Map.of();
        this.status = Objects.requireNonNull(status, "Status cannot be null");
    }

    public static AgentSession create(AgentId agentId) {
        Instant now = Instant.now();
        return new AgentSession(
                SessionId.generate(),
                agentId,
                now,
                now,
                Map.of(),
                SessionStatus.ACTIVE
        );
    }

    public static AgentSession create(AgentId agentId, Map<String, Object> initialData) {
        Instant now = Instant.now();
        return new AgentSession(
                SessionId.generate(),
                agentId,
                now,
                now,
                initialData,
                SessionStatus.ACTIVE
        );
    }

    public AgentSession updateActivity() {
        return new AgentSession(
                id,
                agentId,
                createdAt,
                Instant.now(),
                sessionData,
                status
        );
    }

    public AgentSession putData(String key, Object value) {
        var newData = new java.util.HashMap<>(sessionData);
        newData.put(key, value);
        return new AgentSession(id, agentId, createdAt, Instant.now(), newData, status);
    }

    public AgentSession putAllData(Map<String, Object> data) {
        var newData = new java.util.HashMap<>(sessionData);
        newData.putAll(data);
        return new AgentSession(id, agentId, createdAt, Instant.now(), newData, status);
    }

    public AgentSession close() {
        return new AgentSession(id, agentId, createdAt, Instant.now(), sessionData, SessionStatus.CLOSED);
    }

    public AgentSession pause() {
        return new AgentSession(id, agentId, createdAt, Instant.now(), sessionData, SessionStatus.PAUSED);
    }

    public boolean isActive() {
        return status == SessionStatus.ACTIVE;
    }

    public boolean isExpired(long maxIdleMinutes) {
        long idleSeconds = Instant.now().getEpochSecond() - lastAccessedAt.getEpochSecond();
        return idleSeconds > maxIdleMinutes * 60;
    }

    public Object getData(String key) {
        return sessionData.get(key);
    }

    public SessionId id() { return id; }
    public AgentId agentId() { return agentId; }
    public Instant createdAt() { return createdAt; }
    public Instant lastAccessedAt() { return lastAccessedAt; }
    public Map<String, Object> sessionData() { return sessionData; }
    public SessionStatus status() { return status; }

    public enum SessionStatus {
        ACTIVE, PAUSED, CLOSED, EXPIRED
    }

    private static final class SessionId {
        private final String value;

        private SessionId(String value) {
            this.value = value;
        }

        static SessionId generate() {
            return new SessionId(UUID.randomUUID().toString());
        }

        String value() { return value; }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            SessionId sessionId = (SessionId) o;
            return value.equals(sessionId.value);
        }

        @Override
        public int hashCode() {
            return value.hashCode();
        }
    }
}
