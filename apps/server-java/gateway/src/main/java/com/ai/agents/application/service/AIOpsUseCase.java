package com.ai.agents.application.service;

import com.ai.agents.domain.*;
import com.ai.agents.domain.service.agents.AIOpsAgentService;
import com.ai.agents.domain.workflow.AIOpsWorkflow;
import com.ai.agents.infrastructure.tools.AIOpsTools;
import com.ai.agents.presentation.dto.AgentResponseDto;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.util.List;

/**
 * Use case for AIOps agent operations.
 */
@Service
public class AIOpsUseCase {

    private static final Logger log = LoggerFactory.getLogger(AIOpsUseCase.class);

    private final AIOpsAgentService domainService;
    private final AIOpsTools aiOpsTools;

    public AIOpsUseCase(AIOpsAgentService domainService, AIOpsTools aiOpsTools) {
        this.domainService = domainService;
        this.aiOpsTools = aiOpsTools;
    }

    public Mono<AgentResponseDto> detectAnomaly(String metric, String timeRange, double sensitivity) {
        log.info("Detecting anomaly in metric: {}", metric);
        return aiOpsTools.detectAnomaly(metric, timeRange, sensitivity)
                .map(result -> AgentResponseDto.success(result.content(), AgentType.SUPERVISOR));
    }

    public Mono<AgentResponseDto> createIncident(String title, String severity, String description, List<String> affectedSystems) {
        log.info("Creating incident: {}", title);
        return aiOpsTools.createIncident(title, severity, description, affectedSystems)
                .map(result -> AgentResponseDto.success(result.content(), AgentType.SUPERVISOR));
    }

    public Mono<AgentResponseDto> listIncidents(String status, String severity) {
        log.info("Listing incidents: status={}, severity={}", status, severity);
        return aiOpsTools.listIncidents(status, severity)
                .map(result -> AgentResponseDto.success(result.content(), AgentType.SUPERVISOR));
    }

    public Mono<AgentResponseDto> getSystemHealth() {
        log.info("Getting system health");
        return aiOpsTools.getSystemHealth()
                .map(result -> AgentResponseDto.success(result.content(), AgentType.SUPERVISOR));
    }

    public Mono<AgentResponseDto> rootCauseAnalysis(String incidentId, List<String> affectedServices) {
        log.info("Performing root cause analysis for incident: {}", incidentId);
        return aiOpsTools.rootCauseAnalysis(incidentId, affectedServices)
                .map(result -> AgentResponseDto.success(result.content(), AgentType.SUPERVISOR));
    }

    public Mono<AgentResponseDto> searchLogs(String query, String timeRange, int limit) {
        log.info("Searching logs for: {}", query);
        return aiOpsTools.searchLogs(query, timeRange, limit)
                .map(result -> AgentResponseDto.success(result.content(), AgentType.SUPERVISOR));
    }

    public Mono<AgentResponseDto> acknowledgeAlert(String alertId, String user) {
        log.info("Acknowledging alert: {} by {}", alertId, user);
        return aiOpsTools.acknowledgeAlert(alertId, user)
                .map(result -> AgentResponseDto.success(result.content(), AgentType.SUPERVISOR));
    }

    public Mono<AgentResponseDto> runIncidentResponseWorkflow(String title, String description, String severity, List<String> affectedSystems) {
        log.info("Running incident response workflow for: {}", title);

        Incident.Severity sev = Incident.Severity.valueOf(severity.toUpperCase());
        AIOpsWorkflow workflow = new AIOpsWorkflow(domainService);

        workflow.startIncidentResponse(title, description, sev, affectedSystems);
        workflow.detectAnomalies("system_metric", "5m", 0.5);
        workflow.collectDiagnostics();
        workflow.verifyResolution();

        AIOpsWorkflow.WorkflowSummary summary = workflow.getSummary();

        String output = String.format("""
                Incident Response Workflow Completed

                Workflow ID: %s
                Status: %s
                Completed Nodes: %d
                """,
                summary.workflowId(),
                summary.status(),
                summary.completedNodes().size()
        );

        return Mono.just(AgentResponseDto.success(output, AgentType.SUPERVISOR));
    }
}
