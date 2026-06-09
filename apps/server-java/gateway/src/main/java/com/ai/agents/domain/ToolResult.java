package com.ai.agents.domain;

import java.time.Instant;

/**
 * Tool result value object.
 * Represents the result of a tool execution.
 */
public final class ToolResult {
    private final String content;
    private final boolean success;
    private final String errorMessage;
    private final Instant completedAt;

    private ToolResult(String content, boolean success, String errorMessage, Instant completedAt) {
        this.content = content;
        this.success = success;
        this.errorMessage = errorMessage;
        this.completedAt = completedAt != null ? completedAt : Instant.now();
    }

    public static ToolResult success(String content) {
        return new ToolResult(content, true, null, null);
    }

    public static ToolResult error(String errorMessage) {
        return new ToolResult(null, false, errorMessage, null);
    }

    public boolean isSuccess() { return success; }
    public boolean isError() { return !success; }
    public String content() { return content; }
    public String errorMessage() { return errorMessage; }
    public Instant completedAt() { return completedAt; }

    public String getDisplayContent() {
        if (isSuccess()) {
            return content != null ? content : "";
        } else {
            return "Error: " + (errorMessage != null ? errorMessage : "Unknown error");
        }
    }
}
