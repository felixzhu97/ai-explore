package com.ai.agents.application.service;

import com.ai.agents.domain.*;
import com.ai.agents.domain.service.agents.LLMOpsAgentService;
import com.ai.agents.domain.service.agents.ModelAgentService;
import com.ai.agents.domain.workflow.LLMOpsWorkflow;
import com.ai.agents.infrastructure.tools.ModelTools;
import com.ai.agents.presentation.dto.AgentResponseDto;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.util.Map;

/**
 * Use case for LLMOps and Model agent operations.
 */
@Service
public class LLMOpsUseCase {

    private static final Logger log = LoggerFactory.getLogger(LLMOpsUseCase.class);

    private final LLMOpsAgentService llmOpsService;
    private final ModelAgentService modelService;
    private final ModelTools modelTools;

    public LLMOpsUseCase(LLMOpsAgentService llmOpsService, ModelAgentService modelService, ModelTools modelTools) {
        this.llmOpsService = llmOpsService;
        this.modelService = modelService;
        this.modelTools = modelTools;
    }

    public Mono<AgentResponseDto> registerModel(String modelName, String version, String artifactUri) {
        log.info("Registering model: {}:{}", modelName, version);
        return modelTools.registerModel(modelName, version, artifactUri)
                .map(result -> AgentResponseDto.success(result.content(), AgentType.SUPERVISOR));
    }

    public Mono<AgentResponseDto> listModels(String modelName) {
        log.info("Listing models: {}", modelName);
        return modelTools.listModels(modelName)
                .map(result -> AgentResponseDto.success(result.content(), AgentType.SUPERVISOR));
    }

    public Mono<AgentResponseDto> deployToProduction(String modelName, String version) {
        log.info("Deploying model to production: {}:{}", modelName, version);
        return modelTools.deployToProduction(modelName, version)
                .map(result -> AgentResponseDto.success(result.content(), AgentType.SUPERVISOR));
    }

    public Mono<AgentResponseDto> rollback(String modelName) {
        log.info("Rolling back model: {}", modelName);
        return modelTools.rollback(modelName)
                .map(result -> AgentResponseDto.success(result.content(), AgentType.SUPERVISOR));
    }

    public Mono<AgentResponseDto> recordExperiment(String experimentName, Map<String, Object> metrics) {
        log.info("Recording experiment: {}", experimentName);
        return modelTools.recordExperiment(experimentName, metrics)
                .map(result -> AgentResponseDto.success(result.content(), AgentType.SUPERVISOR));
    }

    public Mono<AgentResponseDto> runTrainingPipeline(String modelName, String experimentName, Map<String, Object> config) {
        log.info("Running training pipeline for: {}", modelName);

        LLMOpsWorkflow workflow = new LLMOpsWorkflow(llmOpsService);
        workflow.trainingPipeline(modelName, experimentName, config);

        Map<String, Object> results = workflow.getResults();

        String output = String.format("""
                Training Pipeline Completed

                Model: %s
                Phase: %s
                Completed Nodes: %d
                """,
                modelName,
                results.get("phase"),
                ((java.util.Set<?>) results.get("completedNodes")).size()
        );

        return Mono.just(AgentResponseDto.success(output, AgentType.SUPERVISOR));
    }

    public Mono<AgentResponseDto> runABTesting(String modelName, String versionA, String versionB, double trafficSplit) {
        log.info("Running A/B test for: {} ({} vs {})", modelName, versionA, versionB);

        LLMOpsWorkflow workflow = new LLMOpsWorkflow(llmOpsService);
        workflow.abTesting(modelName, versionA, versionB, trafficSplit);

        Map<String, Object> results = workflow.getResults();

        String output = String.format("""
                A/B Testing Completed

                Model: %s
                Winner: %s
                """,
                modelName,
                ((Map<?, ?>) results.get("analysis")).get("winner")
        );

        return Mono.just(AgentResponseDto.success(output, AgentType.SUPERVISOR));
    }
}
