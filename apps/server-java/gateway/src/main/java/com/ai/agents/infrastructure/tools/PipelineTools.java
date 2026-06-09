package com.ai.agents.infrastructure.tools;

import com.ai.agents.domain.ToolResult;
import com.ai.agents.domain.service.agents.PipelineAgentService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.util.List;

/**
 * Pipeline tools implementation.
 * Provides DAG pipeline orchestration tools.
 */
@Component
public class PipelineTools {

    private static final Logger log = LoggerFactory.getLogger(PipelineTools.class);
    private final PipelineAgentService domainService;

    public PipelineTools(PipelineAgentService domainService) {
        this.domainService = domainService;
    }

    public Mono<ToolResult> createRun(String pipelineName, List<String> steps) {
        return executeTool("pipeline_create_run", () -> {
            if (!domainService.validateSteps(steps)) {
                return ToolResult.error("Invalid pipeline steps");
            }

            com.ai.agents.domain.PipelineRun run = domainService.createRun(pipelineName, steps);

                    String output = String.format("""
                    Pipeline Run Created

                    Run ID: %s
                    Pipeline: %s
                    Steps: %d
                    Status: %s
                    Started: %s""",
                    run.id(),
                    run.pipelineName(),
                    run.executionOrder().size(),
                    run.status(),
                    run.startedAt()
            );
            return ToolResult.success(output);
        });
    }

    public Mono<ToolResult> getRun(String runId) {
        return executeTool("pipeline_get_run", () -> {
            return domainService.getRun(runId)
                    .map(run -> {
                        StringBuilder output = new StringBuilder();
                        output.append("Pipeline Run: ").append(run.id()).append("\n\n");
                        output.append("Pipeline: ").append(run.pipelineName()).append("\n");
                        output.append("Status: ").append(run.status()).append("\n");
                        output.append("Duration: ").append(run.getDurationSeconds()).append("s\n\n");
                        output.append("Steps:\n");
                        for (var entry : run.stepResults().entrySet()) {
                            output.append("- ").append(entry.getKey()).append(": ")
                                    .append(entry.getValue().status());
                            if (entry.getValue().isFailed()) {
                                output.append(" - ").append(entry.getValue().error());
                            }
                            output.append("\n");
                        }
                        return ToolResult.success(output.toString());
                    })
                    .orElse(ToolResult.error("Pipeline run not found: " + runId));
        });
    }

    public Mono<ToolResult> listRuns(String pipelineName) {
        return executeTool("pipeline_list_runs", () -> {
            List<com.ai.agents.domain.PipelineRun> runs = domainService.listRuns(pipelineName, null);

            if (runs.isEmpty()) {
                return ToolResult.success("No pipeline runs found.");
            }

            StringBuilder output = new StringBuilder("Pipeline Runs:\n\n");
            for (var run : runs) {
                output.append("- ").append(run.id()).append(": ")
                        .append(run.pipelineName()).append(" [").append(run.status()).append("]\n");
            }
            return ToolResult.success(output.toString());
        });
    }

    public Mono<ToolResult> cancelRun(String runId) {
        return executeTool("pipeline_cancel_run", () -> {
            try {
                var run = domainService.cancelRun(runId);
                String output = String.format("Pipeline run %s cancelled", run.id());
                return ToolResult.success(output);
            } catch (IllegalArgumentException e) {
                return ToolResult.error(e.getMessage());
            }
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
