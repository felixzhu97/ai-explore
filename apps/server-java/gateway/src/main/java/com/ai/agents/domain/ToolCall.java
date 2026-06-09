package com.ai.agents.domain;

import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

/**
 * Tool call value object.
 * Represents a single tool invocation during agent execution.
 */
public final class ToolCall {
    private final ToolCallId id;
    private final String toolName;
    private final String arguments;
    private final ToolResult result;
    private final Instant calledAt;
    private final long durationMs;

    private ToolCall(
            ToolCallId id,
            String toolName,
            String arguments,
            ToolResult result,
            Instant calledAt,
            long durationMs
    ) {
        this.id = Objects.requireNonNull(id, "ToolCallId cannot be null");
        this.toolName = Objects.requireNonNull(toolName, "ToolName cannot be null");
        this.arguments = arguments != null ? arguments : "";
        this.result = result;
        this.calledAt = Objects.requireNonNull(calledAt, "CalledAt cannot be null");
        this.durationMs = durationMs;
    }

    public static ToolCall create(String toolName, String arguments) {
        return new ToolCall(
                ToolCallId.generate(),
                toolName,
                arguments,
                null,
                Instant.now(),
                0
        );
    }

    public static ToolCall create(String toolName, Object arguments) {
        String argsStr = arguments instanceof String ? (String) arguments : arguments.toString();
        return create(toolName, argsStr);
    }

    public ToolCall withResult(ToolResult result, long durationMs) {
        return new ToolCall(id, toolName, arguments, result, calledAt, durationMs);
    }

    public ToolCall withResult(String resultContent, boolean success) {
        return withResult(ToolResult.success(resultContent), 0);
    }

    public ToolCall withError(String errorMessage) {
        return withResult(ToolResult.error(errorMessage), 0);
    }

    public boolean hasResult() { return result != null; }
    public boolean isSuccess() { return result != null && result.isSuccess(); }
    public boolean isError() { return result != null && result.isError(); }

    public ToolCallId id() { return id; }
    public String toolName() { return toolName; }
    public String arguments() { return arguments; }
    public ToolResult result() { return result; }
    public Instant calledAt() { return calledAt; }
    public long durationMs() { return durationMs; }

    private static final class ToolCallId {
        private final String value;

        private ToolCallId(String value) { this.value = value; }

        static ToolCallId generate() {
            return new ToolCallId(UUID.randomUUID().toString());
        }

        String value() { return value; }
    }
}
