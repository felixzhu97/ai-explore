package com.ai.agents.domain;

import java.time.Instant;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Execution context value object.
 * Holds execution state for an agent operation.
 */
public final class ExecutionContext {
    private final ExecutionId id;
    private final String operationName;
    private final Map<String, Object> parameters;
    private final Map<String, Object> results;
    private final List<ToolCall> toolCalls;
    private final Instant startedAt;
    private final ExecutionStatus status;
    private final int maxToolCalls;

    private ExecutionContext(
            ExecutionId id,
            String operationName,
            Map<String, Object> parameters,
            Map<String, Object> results,
            List<ToolCall> toolCalls,
            Instant startedAt,
            ExecutionStatus status,
            int maxToolCalls
    ) {
        this.id = Objects.requireNonNull(id, "ExecutionId cannot be null");
        this.operationName = operationName != null ? operationName : "unknown";
        this.parameters = parameters != null ? Map.copyOf(parameters) : Map.of();
        this.results = results != null ? new ConcurrentHashMap<>(results) : new ConcurrentHashMap<>();
        this.toolCalls = toolCalls != null ? new ArrayList<>(toolCalls) : new ArrayList<>();
        this.startedAt = Objects.requireNonNull(startedAt, "StartedAt cannot be null");
        this.status = Objects.requireNonNull(status, "Status cannot be null");
        this.maxToolCalls = maxToolCalls > 0 ? maxToolCalls : 10;
    }

    public static ExecutionContext start(String operationName) {
        return start(operationName, Map.of());
    }

    public static ExecutionContext start(String operationName, Map<String, Object> params) {
        return new ExecutionContext(
                ExecutionId.generate(),
                operationName,
                params,
                new ConcurrentHashMap<>(),
                new ArrayList<>(),
                Instant.now(),
                ExecutionStatus.RUNNING,
                10
        );
    }

    public ExecutionContext addToolCall(ToolCall toolCall) {
        if (toolCalls.size() >= maxToolCalls) {
            throw new IllegalStateException("Maximum tool calls exceeded: " + maxToolCalls);
        }
        List<ToolCall> newCalls = new ArrayList<>(toolCalls);
        newCalls.add(toolCall);
        return new ExecutionContext(
                id, operationName, parameters, results, newCalls,
                startedAt, status, maxToolCalls
        );
    }

    public ExecutionContext addResult(String key, Object value) {
        Map<String, Object> newResults = new ConcurrentHashMap<>(results);
        newResults.put(key, value);
        return new ExecutionContext(
                id, operationName, parameters, newResults, toolCalls,
                startedAt, status, maxToolCalls
        );
    }

    public ExecutionContext addResults(Map<String, Object> values) {
        Map<String, Object> newResults = new ConcurrentHashMap<>(results);
        newResults.putAll(values);
        return new ExecutionContext(
                id, operationName, parameters, newResults, toolCalls,
                startedAt, status, maxToolCalls
        );
    }

    public ExecutionContext complete() {
        return new ExecutionContext(
                id, operationName, parameters, results, toolCalls,
                startedAt, ExecutionStatus.COMPLETED, maxToolCalls
        );
    }

    public ExecutionContext fail(String errorMessage) {
        Map<String, Object> newResults = new ConcurrentHashMap<>(results);
        newResults.put("_error", errorMessage);
        return new ExecutionContext(
                id, operationName, parameters, newResults, toolCalls,
                startedAt, ExecutionStatus.FAILED, maxToolCalls
        );
    }

    public ExecutionContext withMaxToolCalls(int max) {
        return new ExecutionContext(
                id, operationName, parameters, results, toolCalls,
                startedAt, status, max
        );
    }

    public boolean isRunning() { return status == ExecutionStatus.RUNNING; }
    public boolean isCompleted() { return status == ExecutionStatus.COMPLETED; }
    public boolean isFailed() { return status == ExecutionStatus.FAILED; }
    public boolean hasExceededToolCallLimit() { return toolCalls.size() >= maxToolCalls; }

    public long getElapsedMillis() {
        return Instant.now().toEpochMilli() - startedAt.toEpochMilli();
    }

    public Object getParameter(String key) {
        return parameters.get(key);
    }

    public Object getResult(String key) {
        return results.get(key);
    }

    public ExecutionId id() { return id; }
    public String operationName() { return operationName; }
    public Map<String, Object> parameters() { return parameters; }
    public Map<String, Object> results() { return Map.copyOf(results); }
    public List<ToolCall> toolCalls() { return List.copyOf(toolCalls); }
    public Instant startedAt() { return startedAt; }
    public ExecutionStatus status() { return status; }
    public int toolCallCount() { return toolCalls.size(); }
    public int maxToolCalls() { return maxToolCalls; }

    public enum ExecutionStatus {
        RUNNING, COMPLETED, FAILED, TIMEOUT, CANCELLED
    }

    private static final class ExecutionId {
        private final String value;

        private ExecutionId(String value) { this.value = value; }

        static ExecutionId generate() {
            return new ExecutionId(UUID.randomUUID().toString());
        }

        String value() { return value; }
    }
}
