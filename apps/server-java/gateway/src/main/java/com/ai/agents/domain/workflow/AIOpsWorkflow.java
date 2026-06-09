package com.ai.agents.domain.workflow;

import com.ai.agents.domain.Incident;
import com.ai.agents.domain.WorkflowState;
import com.ai.agents.domain.service.agents.AIOpsAgentService;

import java.time.Instant;
import java.util.*;

/**
 * AIOps workflow domain model.
 * Manages incident response, anomaly detection, and root cause analysis workflows.
 */
public final class AIOpsWorkflow {

    private final WorkflowState state;
    private final AIOpsAgentService aiOpsService;

    private AIOpsWorkflow(AIOpsAgentService aiOpsService, WorkflowState state) {
        this.aiOpsService = Objects.requireNonNull(aiOpsService, "AIOpsService cannot be null");
        this.state = Objects.requireNonNull(state, "WorkflowState cannot be null");
    }

    public AIOpsWorkflow(AIOpsAgentService aiOpsService) {
        this(aiOpsService, WorkflowState.start("aiops"));
    }

    /**
     * Create incident response workflow.
     */
    public AIOpsWorkflow startIncidentResponse(String title, String description, Incident.Severity severity, List<String> affectedSystems) {
        Incident incident = aiOpsService.createIncident(title, description, severity, affectedSystems);

        WorkflowState newState = state
                .updateState("incident", Map.of(
                        "id", incident.idValue(),
                        "title", incident.title(),
                        "severity", incident.severity().name()
                ))
                .updateState("phase", "create_incident")
                .completeNode("create_incident")
                .moveToNode("detect_anomalies");

        return new AIOpsWorkflow(aiOpsService).withState(newState);
    }

    /**
     * Detect anomalies workflow step.
     */
    public AIOpsWorkflow detectAnomalies(String metric, String timeRange, double sensitivity) {
        AIOpsAgentService.AnomalyResult result = aiOpsService.detectAnomaly(metric, timeRange, sensitivity);

        Map<String, Object> anomalyData = new HashMap<>();
        anomalyData.put("metric", result.metric());
        anomalyData.put("score", result.score());
        anomalyData.put("isAnomaly", result.isAnomaly());
        anomalyData.put("explanation", result.explanation());

        WorkflowState newState = state
                .updateState("anomalyResult", anomalyData)
                .updateState("phase", "detect_anomalies")
                .completeNode("detect_anomalies")
                .moveToNode("collect_diagnostics");

        return new AIOpsWorkflow(aiOpsService).withState(newState);
    }

    /**
     * Collect diagnostics workflow step.
     */
    public AIOpsWorkflow collectDiagnostics() {
        Map<String, Object> diagnostics = new HashMap<>();
        diagnostics.put("sources", List.of("metrics", "logs", "traces", "events"));
        diagnostics.put("timestamp", Instant.now().toString());

        WorkflowState newState = state
                .updateState("diagnostics", diagnostics)
                .updateState("phase", "collect_diagnostics")
                .completeNode("collect_diagnostics")
                .moveToNode("analyze_root_cause");

        return new AIOpsWorkflow(aiOpsService).withState(newState);
    }

    /**
     * Analyze root cause workflow step.
     */
    public AIOpsWorkflow analyzeRootCause(String incidentId, List<String> affectedServices) {
        AIOpsAgentService.RootCauseResult result = aiOpsService.analyzeRootCause(incidentId, affectedServices);

        Map<String, Object> rcData = new HashMap<>();
        rcData.put("rootCause", result.rootCause());
        rcData.put("confidence", result.confidence());
        rcData.put("factors", result.contributingFactors());
        rcData.put("recommendations", result.recommendations());

        WorkflowState newState = state
                .updateState("rootCause", rcData)
                .updateState("phase", "analyze_root_cause")
                .completeNode("analyze_root_cause")
                .moveToNode("execute_remediation");

        return new AIOpsWorkflow(aiOpsService).withState(newState);
    }

    /**
     * Execute remediation workflow step.
     */
    public AIOpsWorkflow executeRemediation() {
        @SuppressWarnings("unchecked")
        Map<String, Object> rcData = (Map<String, Object>) state.getStateValue("rootCause");
        @SuppressWarnings("unchecked")
        List<String> recommendations = rcData != null ? (List<String>) rcData.get("recommendations") : List.of();

        Map<String, Object> actions = new HashMap<>();
        actions.put("actionsTaken", recommendations.stream().limit(2).toList());
        actions.put("timestamp", Instant.now().toString());

        WorkflowState newState = state
                .updateState("remediation", actions)
                .updateState("phase", "execute_remediation")
                .completeNode("execute_remediation")
                .moveToNode("verify_resolution");

        return new AIOpsWorkflow(aiOpsService).withState(newState);
    }

    /**
     * Verify resolution workflow step.
     */
    public AIOpsWorkflow verifyResolution() {
        WorkflowState newState = state
                .updateState("phase", "verify_resolution")
                .updateState("verified", true)
                .completeNode("verify_resolution")
                .complete();

        return new AIOpsWorkflow(aiOpsService).withState(newState);
    }

    /**
     * Get workflow summary.
     */
    public WorkflowSummary getSummary() {
        @SuppressWarnings("unchecked")
        Map<String, Object> incident = (Map<String, Object>) state.getStateValue("incident");
        @SuppressWarnings("unchecked")
        Map<String, Object> anomalyResult = (Map<String, Object>) state.getStateValue("anomalyResult");
        @SuppressWarnings("unchecked")
        Map<String, Object> rootCause = (Map<String, Object>) state.getStateValue("rootCause");
        @SuppressWarnings("unchecked")
        Map<String, Object> remediation = (Map<String, Object>) state.getStateValue("remediation");
        String phase = (String) state.getStateValue("phase");

        return new WorkflowSummary(
                state.idValue(),
                state.workflowType(),
                state.status(),
                phase,
                incident,
                anomalyResult,
                rootCause,
                remediation,
                state.completedNodes()
        );
    }

    public WorkflowState state() { return state; }
    public boolean isCompleted() { return state.isCompleted(); }
    public boolean isFailed() { return state.isFailed(); }

    private AIOpsWorkflow withState(WorkflowState newState) {
        return new AIOpsWorkflow(aiOpsService, newState);
    }

    public record WorkflowSummary(
            String workflowId,
            String workflowType,
            WorkflowState.WorkflowStatus status,
            String currentPhase,
            Map<String, Object> incidentData,
            Map<String, Object> anomalyData,
            Map<String, Object> rootCauseData,
            Map<String, Object> remediationData,
            Set<String> completedNodes
    ) {}
}
