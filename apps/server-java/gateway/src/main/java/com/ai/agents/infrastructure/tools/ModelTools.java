package com.ai.agents.infrastructure.tools;

import com.ai.agents.domain.ToolResult;
import com.ai.agents.domain.service.agents.FeatureStoreAgentService;
import com.ai.agents.domain.service.agents.LLMOpsAgentService;
import com.ai.agents.domain.service.agents.ModelAgentService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.util.Map;

/**
 * Model and Feature Store tools implementation.
 */
@Component
public class ModelTools {

    private static final Logger log = LoggerFactory.getLogger(ModelTools.class);
    private final ModelAgentService modelService;
    private final LLMOpsAgentService llmOpsService;
    private final FeatureStoreAgentService featureStoreService;

    public ModelTools(ModelAgentService modelService, LLMOpsAgentService llmOpsService, FeatureStoreAgentService featureStoreService) {
        this.modelService = modelService;
        this.llmOpsService = llmOpsService;
        this.featureStoreService = featureStoreService;
    }

    public Mono<ToolResult> registerModel(String modelName, String version, String artifactUri) {
        return executeTool("model_register", () -> {
            var model = modelService.register(modelName, version, artifactUri);
            return ToolResult.success(String.format("Model registered: %s:%s", modelName, version));
        });
    }

    public Mono<ToolResult> listModels(String modelName) {
        return executeTool("model_list", () -> {
            var models = modelService.listModels(modelName);
            StringBuilder output = new StringBuilder("Models:\n\n");
            for (var model : models) {
                output.append("- ").append(model.modelName()).append(":").append(model.version())
                        .append(" [").append(model.status()).append("]\n");
            }
            return ToolResult.success(output.toString());
        });
    }

    public Mono<ToolResult> deployToProduction(String modelName, String version) {
        return executeTool("model_deploy", () -> {
            try {
                var model = modelService.deployToProduction(modelName, version);
                return ToolResult.success(String.format("Model %s:%s deployed to production", modelName, version));
            } catch (IllegalArgumentException e) {
                return ToolResult.error(e.getMessage());
            }
        });
    }

    public Mono<ToolResult> rollback(String modelName) {
        return executeTool("model_rollback", () -> {
            try {
                var result = modelService.rollback(modelName);
                return ToolResult.success(String.format("Rolled back %s from %s to %s",
                        result.modelName(), result.fromVersion(), result.toVersion()));
            } catch (Exception e) {
                return ToolResult.error(e.getMessage());
            }
        });
    }

    public Mono<ToolResult> recordExperiment(String experimentName, Map<String, Object> metrics) {
        return executeTool("llmops_record_experiment", () -> {
            var result = llmOpsService.recordExperiment(experimentName, metrics, "completed");
            return ToolResult.success(String.format("Experiment recorded: %s", experimentName));
        });
    }

    public Mono<ToolResult> createFeature(String name, String entityType, String dataType, String description) {
        return executeTool("feature_create", () -> {
            var feature = featureStoreService.registerFeature(name, entityType, dataType, description);
            return ToolResult.success(String.format("Feature registered: %s", name));
        });
    }

    public Mono<ToolResult> materializeFeature(String featureName, String entityId) {
        return executeTool("feature_materialize", () -> {
            var result = featureStoreService.materialize(featureName, entityId);
            return ToolResult.success(String.format("Materialized %d records for %s:%s",
                    result.recordCount(), featureName, entityId));
        });
    }

    private <T> Mono<ToolResult> executeTool(String toolName, ToolExecutor executor) {
        try {
            ToolResult result = executor.execute();
            log.debug("Tool {} executed successfully", toolName);
            return Mono.just(result);
        } catch (Exception e) {
            log.error("Tool {} failed: {}", toolName, e.getMessage());
            return Mono.just(ToolResult.error(e.getMessage()));
        }
    }

    @FunctionalInterface
    private interface ToolExecutor {
        ToolResult execute();
    }
}
