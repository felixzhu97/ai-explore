package com.ai.agents.domain;

import java.time.Instant;
import java.util.Objects;

/**
 * Message value object representing a single message in a conversation.
 */
public final class Message {
    private final MessageId id;
    private final AgentId agentId;
    private final String content;
    private final MessageRole role;
    private final Instant timestamp;

    private Message(MessageId id, AgentId agentId, String content, MessageRole role, Instant timestamp) {
        this.id = Objects.requireNonNull(id, "MessageId cannot be null");
        this.agentId = agentId;
        this.content = Objects.requireNonNull(content, "Content cannot be null");
        this.role = Objects.requireNonNull(role, "Role cannot be null");
        this.timestamp = timestamp != null ? timestamp : Instant.now();
    }

    public static Message of(String content, MessageRole role) {
        return new Message(MessageId.generate(), null, content, role, null);
    }

    public static Message fromUser(String content) {
        return new Message(MessageId.generate(), null, content, MessageRole.USER, null);
    }

    public static Message fromAssistant(AgentId agentId, String content) {
        return new Message(MessageId.generate(), agentId, content, MessageRole.ASSISTANT, null);
    }

    public static Message system(String content) {
        return new Message(MessageId.generate(), null, content, MessageRole.SYSTEM, null);
    }

    public MessageId id() { return id; }
    public AgentId agentId() { return agentId; }
    public String content() { return content; }
    public MessageRole role() { return role; }
    public Instant timestamp() { return timestamp; }

    public boolean isFromUser() { return role == MessageRole.USER; }
    public boolean isFromAssistant() { return role == MessageRole.ASSISTANT; }
    public boolean isSystem() { return role == MessageRole.SYSTEM; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Message message = (Message) o;
        return id.equals(message.id);
    }

    @Override
    public int hashCode() {
        return id.hashCode();
    }

    public enum MessageRole {
        USER, ASSISTANT, SYSTEM
    }

    private static final class MessageId {
        private final String value;

        private MessageId(String value) {
            this.value = value;
        }

        static MessageId generate() {
            return new MessageId(java.util.UUID.randomUUID().toString());
        }

        String value() { return value; }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            MessageId messageId = (MessageId) o;
            return value.equals(messageId.value);
        }

        @Override
        public int hashCode() {
            return value.hashCode();
        }
    }
}
