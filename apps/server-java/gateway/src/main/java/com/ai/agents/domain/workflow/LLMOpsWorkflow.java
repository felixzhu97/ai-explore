package com.ai.agents.domain.workflow;

import com.ai.agents.domain.*;
import com.ai.agents.domain.service.agents.LLMOpsAgentService;

import java.util.*;

/**
 * LLMOps workflow domain model.
 * Manages ML model training, evaluation, and deployment workflows.
 */
public final class LLMOpsWorkflow {

    private final LLMOpsAgentService llmOpsService;
    private final WorkflowState state;

    private LLMOpsWorkflow(LLMOpsAgentService llmOpsService, WorkflowState state) {
        this.llmOpsService = Objects.requireNonNull(llmOpsService, "LLMOpsService cannot be null");
        this.state = Objects.requireNonNull(state, "WorkflowState cannot be null");
    }

    public LLMOpsWorkflow(LLMOpsAgentService llmOpsService) {
        this(llmOpsService, WorkflowState.start("llmops"));
    }

    private LLMOpsWorkflow withState(WorkflowState newState) {
        return new LLMOpsWorkflow(llmOpsService, newState);
    }

    public LLMOpsWorkflow registerModel(String modelName, String version, String artifactUri) {
        ModelVersion model = llmOpsService.registerModel(modelName, version, artifactUri);
        Map<String, Object> modelData = Map.of(
                "id", model.id(),
                "name", model.modelName(),
                "version", model.version(),
                "status", model.status().name()
        );
        return withState(state
                .updateState("model", modelData)
                .updateState("phase", "register")
                .completeNode("register")
                .complete());
    }

    public LLMOpsWorkflow trainingPipeline(String modelName, String experimentName, Map<String, Object> config) {
        LLMOpsWorkflow current = withState(state.moveToNode("prepare_data"));
        current = current.withState(current.state.updateState("dataPrep", Map.of("dataset", "prepared", "samples", 10000))
                .completeNode("prepare_data").moveToNode("train"));
        current = current.withState(current.state.updateState("training", Map.of("loss", 0.15, "iterations", 1000))
                .completeNode("train").moveToNode("evaluate"));
        return current.withState(current.state.updateState("evaluation", Map.of("accuracy", 0.92, "f1", 0.89))
                .updateState("phase", "training_pipeline").completeNode("evaluate").complete());
    }

    public LLMOpsWorkflow abTesting(String modelName, String versionA, String versionB, double trafficSplit) {
        LLMOpsWorkflow current = withState(state
                .updateState("testConfig", Map.of("modelA", versionA, "modelB", versionB, "trafficSplit", trafficSplit))
                .moveToNode("deploy_candidates"));
        ModelVersion modelA = llmOpsService.getModel(modelName, versionA).orElse(null);
        ModelVersion modelB = llmOpsService.getModel(modelName, versionB).orElse(null);
        current = current.withState(current.state
                .updateState("deployments", Map.of("modelADeployed", modelA != null, "modelBDeployed", modelB != null))
                .completeNode("deploy_candidates").moveToNode("collect_metrics"));
        current = current.withState(current.state
                .updateState("abMetrics", Map.of(
                        "modelA", Map.of("requests", 1000, "latency", 45.2),
                        "modelB", Map.of("requests", 1000, "latency", 42.8)))
                .completeNode("collect_metrics").moveToNode("analyze_results"));
        String winner = trafficSplit > 0.5 ? versionA : versionB;
        return current.withState(current.state
                .updateState("analysis", Map.of("winner", winner, "confidence", 0.95))
                .updateState("phase", "ab_testing").completeNode("analyze_results").complete());
    }

    public LLMOpsWorkflow canaryDeployment(String modelName, String newVersion, double initialWeight) {
        String currentVersion = llmOpsService.listModels(modelName, null).stream()
                .filter(ModelVersion::isProduction)
                .map(ModelVersion::version)
                .findFirst().orElse("unknown");
        LLMOpsWorkflow current = withState(state
                .updateState("canaryConfig", Map.of("model", modelName, "currentVersion", currentVersion,
                        "newVersion", newVersion, "initialWeight", initialWeight))
                .moveToNode("deploy_canary"));
        current = current.withState(current.state
                .updateState("canaryStatus", Map.of("version", newVersion, "weight", initialWeight, "healthy", true))
                .completeNode("deploy_canary").moveToNode("monitor_canary"));
        Map<String, Object> monitoring = Map.of("errorRate", 0.01, "latencyP99", 150, "passed", true);
        boolean promote = ((Number) monitoring.get("errorRate")).doubleValue() < 0.05;
        String action = promote ? "promote" : "rollback";
        return current.withState(current.state
                .updateState("monitoring", monitoring)
                .updateState("action", action)
                .updateState("phase", "canary_deployment")
                .completeNode("monitor_canary")
                .completeNode("promote_or_rollback")
                .complete());
    }

    public LLMOpsWorkflow rollback(String modelName) {
        LLMOpsWorkflow current = withState(state.moveToNode("identify_previous"));
        llmOpsService.recordExperiment("rollback_" + modelName, Map.of("action", "rollback"), "completed");
        ModelVersion rolledBack = llmOpsService.rollback(modelName);
        return current.withState(current.state
                .updateState("rollback", Map.of("toVersion", rolledBack.version(), "timestamp", java.time.Instant.now().toString()))
                .updateState("phase", "rollback")
                .completeNode("identify_previous")
                .complete());
    }

    public Map<String, Object> getResults() {
        Map<String, Object> results = new HashMap<>();
        results.put("workflowId", state.id());
        results.put("phase", state.getStateValue("phase"));
        results.put("model", state.getStateValue("model"));
        results.put("training", state.getStateValue("training"));
        results.put("evaluation", state.getStateValue("evaluation"));
        results.put("analysis", state.getStateValue("analysis"));
        results.put("canaryStatus", state.getStateValue("canaryStatus"));
        results.put("rollback", state.getStateValue("rollback"));
        results.put("completedNodes", state.completedNodes());
        return results;
    }

    public WorkflowState state() { return state; }
    public boolean isCompleted() { return state.isCompleted(); }
    public String getPhase() { return (String) state.getStateValue("phase"); }
}
