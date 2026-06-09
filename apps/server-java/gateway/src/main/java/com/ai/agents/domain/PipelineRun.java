package com.ai.agents.domain;

import java.time.Instant;
import java.util.*;

/**
 * Pipeline run value object.
 * Represents a DAG pipeline execution.
 */
public final class PipelineRun {
    private final String id;
    private final String pipelineName;
    private final PipelineStatus status;
    private final Map<String, StepResult> stepResults;
    private final List<String> executionOrder;
    private final Instant startedAt;
    private final Instant completedAt;

    private PipelineRun(
            String id,
            String pipelineName,
            PipelineStatus status,
            Map<String, StepResult> stepResults,
            List<String> executionOrder,
            Instant startedAt,
            Instant completedAt
    ) {
        this.id = Objects.requireNonNull(id, "Id cannot be null");
        this.pipelineName = Objects.requireNonNull(pipelineName, "PipelineName cannot be null");
        this.status = Objects.requireNonNull(status, "Status cannot be null");
        this.stepResults = stepResults != null ? new LinkedHashMap<>(stepResults) : new LinkedHashMap<>();
        this.executionOrder = executionOrder != null ? new ArrayList<>(executionOrder) : new ArrayList<>();
        this.startedAt = Objects.requireNonNull(startedAt, "StartedAt cannot be null");
        this.completedAt = completedAt;
    }

    public static PipelineRun start(String pipelineName, List<String> steps) {
        return new PipelineRun(
                UUID.randomUUID().toString(),
                pipelineName,
                PipelineStatus.RUNNING,
                new LinkedHashMap<>(),
                steps != null ? new ArrayList<>(steps) : new ArrayList<>(),
                Instant.now(),
                null
        );
    }

    public PipelineRun recordStepStart(String stepName) {
        Map<String, StepResult> newResults = new LinkedHashMap<>(stepResults);
        newResults.put(stepName, StepResult.running());
        return new PipelineRun(
                id, pipelineName, status, newResults, executionOrder, startedAt, completedAt
        );
    }

    public PipelineRun recordStepComplete(String stepName, Object output) {
        Map<String, StepResult> newResults = new LinkedHashMap<>(stepResults);
        newResults.put(stepName, StepResult.success(output));
        return new PipelineRun(
                id, pipelineName, status, newResults, executionOrder, startedAt, completedAt
        );
    }

    public PipelineRun recordStepFailed(String stepName, String error) {
        Map<String, StepResult> newResults = new LinkedHashMap<>(stepResults);
        newResults.put(stepName, StepResult.failed(error));
        return new PipelineRun(
                id, pipelineName, PipelineStatus.FAILED, newResults, executionOrder, startedAt, Instant.now()
        );
    }

    public PipelineRun complete() {
        boolean allSuccess = stepResults.values().stream()
                .allMatch(r -> r.status() == StepStatus.SUCCESS);
        return new PipelineRun(
                id, pipelineName,
                allSuccess ? PipelineStatus.COMPLETED : PipelineStatus.FAILED,
                stepResults, executionOrder, startedAt, Instant.now()
        );
    }

    public PipelineRun cancel() {
        return new PipelineRun(
                id, pipelineName, PipelineStatus.CANCELLED,
                stepResults, executionOrder, startedAt, Instant.now()
        );
    }

    public String id() { return id; }
    public String pipelineName() { return pipelineName; }
    public PipelineStatus status() { return status; }
    public Map<String, StepResult> stepResults() { return Map.copyOf(stepResults); }
    public List<String> executionOrder() { return List.copyOf(executionOrder); }
    public Instant startedAt() { return startedAt; }
    public Instant completedAt() { return completedAt; }

    public boolean isRunning() { return status == PipelineStatus.RUNNING; }
    public boolean isCompleted() { return status == PipelineStatus.COMPLETED; }
    public boolean isFailed() { return status == PipelineStatus.FAILED; }
    public boolean isStepCompleted(String stepName) { return stepResults.containsKey(stepName); }
    public boolean isStepFailed(String stepName) { return stepResults.get(stepName) != null && stepResults.get(stepName).isFailed(); }

    public long getDurationSeconds() {
        Instant end = completedAt != null ? completedAt : Instant.now();
        return end.getEpochSecond() - startedAt.getEpochSecond();
    }

    public enum PipelineStatus {
        RUNNING, COMPLETED, FAILED, CANCELLED
    }

    public record StepResult(StepStatus status, Object output, String error, Instant timestamp) {
        public static StepResult running() {
            return new StepResult(StepStatus.RUNNING, null, null, Instant.now());
        }
        public static StepResult success(Object output) {
            return new StepResult(StepStatus.SUCCESS, output, null, Instant.now());
        }
        public static StepResult failed(String error) {
            return new StepResult(StepStatus.FAILED, null, error, Instant.now());
        }
        public boolean isSuccess() { return status == StepStatus.SUCCESS; }
        public boolean isFailed() { return status == StepStatus.FAILED; }
    }

    public enum StepStatus {
        PENDING, RUNNING, SUCCESS, FAILED, SKIPPED
    }
}
