package com.ai.agents.domain;

import java.time.Instant;
import java.util.*;

/**
 * Workflow state value object.
 * Manages state for LangGraph-style workflow execution.
 */
public final class WorkflowState {
    private final WorkflowId id;
    private final String workflowType;
    private final Map<String, Object> state;
    private final List<String> currentNodes;
    private final List<String> pendingNodes;
    private final Set<String> completedNodes;
    private final Instant createdAt;
    private final Instant lastUpdatedAt;
    private final WorkflowStatus status;

    private WorkflowState(
            WorkflowId id,
            String workflowType,
            Map<String, Object> state,
            List<String> currentNodes,
            List<String> pendingNodes,
            Set<String> completedNodes,
            Instant createdAt,
            Instant lastUpdatedAt,
            WorkflowStatus status
    ) {
        this.id = Objects.requireNonNull(id, "WorkflowId cannot be null");
        this.workflowType = workflowType != null ? workflowType : "generic";
        this.state = state != null ? new HashMap<>(state) : new HashMap<>();
        this.currentNodes = currentNodes != null ? new ArrayList<>(currentNodes) : new ArrayList<>();
        this.pendingNodes = pendingNodes != null ? new ArrayList<>(pendingNodes) : new ArrayList<>();
        this.completedNodes = completedNodes != null ? new HashSet<>(completedNodes) : new HashSet<>();
        this.createdAt = Objects.requireNonNull(createdAt, "CreatedAt cannot be null");
        this.lastUpdatedAt = Objects.requireNonNull(lastUpdatedAt, "LastUpdatedAt cannot be null");
        this.status = Objects.requireNonNull(status, "Status cannot be null");
    }

    public static WorkflowState start(String workflowType) {
        return start(workflowType, Map.of());
    }

    public static WorkflowState start(String workflowType, Map<String, Object> initialState) {
        Instant now = Instant.now();
        return new WorkflowState(
                WorkflowId.generate(),
                workflowType,
                new HashMap<>(initialState),
                List.of("start"),
                List.of(),
                Set.of(),
                now,
                now,
                WorkflowStatus.RUNNING
        );
    }

    public WorkflowState updateState(String key, Object value) {
        Map<String, Object> newState = new HashMap<>(state);
        newState.put(key, value);
        return new WorkflowState(
                id, workflowType, newState, currentNodes, pendingNodes, completedNodes,
                createdAt, Instant.now(), status
        );
    }

    public WorkflowState updateState(Map<String, Object> updates) {
        Map<String, Object> newState = new HashMap<>(state);
        newState.putAll(updates);
        return new WorkflowState(
                id, workflowType, newState, currentNodes, pendingNodes, completedNodes,
                createdAt, Instant.now(), status
        );
    }

    public WorkflowState moveToNode(String nodeName) {
        return new WorkflowState(
                id, workflowType, state,
                List.of(nodeName),
                pendingNodes,
                completedNodes,
                createdAt, Instant.now(), status
        );
    }

    public WorkflowState completeNode(String nodeName) {
        Set<String> newCompleted = new HashSet<>(completedNodes);
        newCompleted.add(nodeName);
        List<String> newCurrent = new ArrayList<>(currentNodes);
        newCurrent.remove(nodeName);
        return new WorkflowState(
                id, workflowType, state, newCurrent, pendingNodes, newCompleted,
                createdAt, Instant.now(), status
        );
    }

    public WorkflowState addPendingNode(String nodeName) {
        List<String> newPending = new ArrayList<>(pendingNodes);
        newPending.add(nodeName);
        return new WorkflowState(
                id, workflowType, state, currentNodes, newPending, completedNodes,
                createdAt, Instant.now(), status
        );
    }

    public WorkflowState nextPending() {
        if (pendingNodes.isEmpty()) {
            return complete();
        }
        String next = pendingNodes.get(0);
        List<String> newPending = new ArrayList<>(pendingNodes);
        newPending.remove(0);
        return new WorkflowState(
                id, workflowType, state, List.of(next), newPending, completedNodes,
                createdAt, Instant.now(), status
        );
    }

    public WorkflowState complete() {
        Set<String> newCompleted = new HashSet<>(completedNodes);
        newCompleted.addAll(currentNodes);
        return new WorkflowState(
                id, workflowType, state, List.of(), List.of(), newCompleted,
                createdAt, Instant.now(), WorkflowStatus.COMPLETED
        );
    }

    public WorkflowState fail() {
        return new WorkflowState(
                id, workflowType, state, currentNodes, pendingNodes, completedNodes,
                createdAt, Instant.now(), WorkflowStatus.FAILED
        );
    }

    public boolean isRunning() { return status == WorkflowStatus.RUNNING; }
    public boolean isCompleted() { return status == WorkflowStatus.COMPLETED; }
    public boolean isFailed() { return status == WorkflowStatus.FAILED; }
    public boolean hasPendingNodes() { return !pendingNodes.isEmpty(); }
    public boolean isNodeCompleted(String nodeName) { return completedNodes.contains(nodeName); }

    public Object getStateValue(String key) { return state.get(key); }

    public WorkflowId id() { return id; }
    public String idValue() { return id.value(); }
    public String workflowType() { return workflowType; }
    public Map<String, Object> state() { return Map.copyOf(state); }
    public List<String> currentNodes() { return List.copyOf(currentNodes); }
    public List<String> pendingNodes() { return List.copyOf(pendingNodes); }
    public Set<String> completedNodes() { return Set.copyOf(completedNodes); }
    public Instant createdAt() { return createdAt; }
    public Instant lastUpdatedAt() { return lastUpdatedAt; }
    public WorkflowStatus status() { return status; }

    public enum WorkflowStatus {
        RUNNING, COMPLETED, FAILED, CANCELLED
    }

    private static final class WorkflowId {
        private final String value;

        private WorkflowId(String value) { this.value = value; }

        static WorkflowId generate() {
            return new WorkflowId(UUID.randomUUID().toString());
        }

        String value() { return value; }
    }
}
