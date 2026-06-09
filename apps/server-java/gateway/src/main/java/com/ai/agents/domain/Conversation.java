package com.ai.agents.domain;

import java.time.Instant;
import java.util.List;
import java.util.Map;

/**
 * Conversation aggregate root.
 * Manages a conversation context with messages.
 */
public final class Conversation {
    private final ConversationId id;
    private final AgentId primaryAgentId;
    private final List<Message> messages;
    private final Map<String, Object> context;
    private final Instant createdAt;
    private final Instant lastUpdatedAt;

    private Conversation(
            ConversationId id,
            AgentId primaryAgentId,
            List<Message> messages,
            Map<String, Object> context,
            Instant createdAt
    ) {
        this.id = id;
        this.primaryAgentId = primaryAgentId;
        this.messages = messages instanceof List ? new java.util.ArrayList<>(messages) : messages;
        this.context = context instanceof Map ? new java.util.HashMap<>(context) : context;
        this.createdAt = createdAt;
        this.lastUpdatedAt = createdAt;
    }

    public static Conversation start(AgentId agentId, Message initialMessage) {
        if (initialMessage == null) {
            throw new IllegalArgumentException("Initial message cannot be null");
        }
        List<Message> messages = new java.util.ArrayList<>();
        messages.add(initialMessage);
        return new Conversation(
                ConversationId.generate(),
                agentId,
                messages,
                new java.util.HashMap<>(),
                Instant.now()
        );
    }

    public static Conversation create(AgentId agentId) {
        return new Conversation(
                ConversationId.generate(),
                agentId,
                new java.util.ArrayList<>(),
                new java.util.HashMap<>(),
                Instant.now()
        );
    }

    public Conversation addMessage(Message message) {
        if (message == null) {
            throw new IllegalArgumentException("Message cannot be null");
        }
        List<Message> newMessages = new java.util.ArrayList<>(this.messages);
        newMessages.add(message);
        return new Conversation(id, primaryAgentId, newMessages, context, createdAt);
    }

    public boolean canContinue() {
        return messages.size() < 100;
    }

    public List<Message> getContext() {
        return java.util.Collections.unmodifiableList(messages);
    }

    public Map<String, Object> getMetadata() {
        return java.util.Collections.unmodifiableMap(context);
    }

    public Conversation withMetadata(String key, Object value) {
        Map<String, Object> newContext = new java.util.HashMap<>(context);
        newContext.put(key, value);
        return new Conversation(id, primaryAgentId, messages, newContext, createdAt);
    }

    public ConversationId id() { return id; }
    public AgentId primaryAgentId() { return primaryAgentId; }
    public List<Message> messages() { return getContext(); }
    public Instant createdAt() { return createdAt; }
    public Instant lastUpdatedAt() { return lastUpdatedAt; }
    public int messageCount() { return messages.size(); }

    private static final class ConversationId {
        private final String value;

        private ConversationId(String value) {
            this.value = value;
        }

        static ConversationId generate() {
            return new ConversationId(java.util.UUID.randomUUID().toString());
        }

        String value() { return value; }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            ConversationId that = (ConversationId) o;
            return value.equals(that.value);
        }

        @Override
        public int hashCode() {
            return value.hashCode();
        }
    }
}
