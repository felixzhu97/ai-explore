package com.ai.agents.application.service;

import com.ai.agents.domain.*;
import com.ai.agents.domain.service.agents.PipelineAgentService;
import com.ai.agents.infrastructure.tools.PipelineTools;
import com.ai.agents.presentation.dto.AgentResponseDto;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.util.List;

/**
 * Use case for Pipeline agent operations.
 */
@Service
public class PipelineUseCase {

    private static final Logger log = LoggerFactory.getLogger(PipelineUseCase.class);

    private final PipelineAgentService domainService;
    private final PipelineTools pipelineTools;

    public PipelineUseCase(PipelineAgentService domainService, PipelineTools pipelineTools) {
        this.domainService = domainService;
        this.pipelineTools = pipelineTools;
    }

    public Mono<AgentResponseDto> createRun(String pipelineName, List<String> steps) {
        log.info("Creating pipeline run: {}", pipelineName);
        return pipelineTools.createRun(pipelineName, steps)
                .map(result -> AgentResponseDto.success(result.content(), AgentType.SUPERVISOR));
    }

    public Mono<AgentResponseDto> getRun(String runId) {
        log.info("Getting pipeline run: {}", runId);
        return pipelineTools.getRun(runId)
                .map(result -> AgentResponseDto.success(result.content(), AgentType.SUPERVISOR));
    }

    public Mono<AgentResponseDto> listRuns(String pipelineName) {
        log.info("Listing pipeline runs: {}", pipelineName);
        return pipelineTools.listRuns(pipelineName)
                .map(result -> AgentResponseDto.success(result.content(), AgentType.SUPERVISOR));
    }

    public Mono<AgentResponseDto> cancelRun(String runId) {
        log.info("Cancelling pipeline run: {}", runId);
        return pipelineTools.cancelRun(runId)
                .map(result -> AgentResponseDto.success(result.content(), AgentType.SUPERVISOR));
    }
}
