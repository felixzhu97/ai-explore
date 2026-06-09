package com.ai.agents.application.service;

import com.ai.agents.domain.*;
import com.ai.agents.domain.service.agents.MonitoringAgentService;
import com.ai.agents.infrastructure.tools.MonitoringTools;
import com.ai.agents.presentation.dto.AgentResponseDto;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

/**
 * Use case for Monitoring agent operations.
 */
@Service
public class MonitoringUseCase {

    private static final Logger log = LoggerFactory.getLogger(MonitoringUseCase.class);

    private final MonitoringAgentService domainService;
    private final MonitoringTools monitoringTools;

    public MonitoringUseCase(MonitoringAgentService domainService, MonitoringTools monitoringTools) {
        this.domainService = domainService;
        this.monitoringTools = monitoringTools;
    }

    public Mono<AgentResponseDto> queryMetrics(String metric, String timeRange, String aggregation) {
        log.info("Querying metric: {}", metric);
        return monitoringTools.queryMetrics(metric, timeRange, aggregation)
                .map(result -> AgentResponseDto.success(result.content(), AgentType.SUPERVISOR));
    }

    public Mono<AgentResponseDto> createAlert(String name, String metric, String condition, double threshold) {
        log.info("Creating alert: {}", name);
        return monitoringTools.createAlert(name, metric, condition, threshold)
                .map(result -> AgentResponseDto.success(result.content(), AgentType.SUPERVISOR));
    }

    public Mono<AgentResponseDto> listAlerts(String status) {
        log.info("Listing alerts: {}", status);
        return monitoringTools.listAlerts(status)
                .map(result -> AgentResponseDto.success(result.content(), AgentType.SUPERVISOR));
    }

    public Mono<AgentResponseDto> fireAlert(String name, String message) {
        log.info("Firing alert: {}", name);
        return monitoringTools.fireAlert(name, message)
                .map(result -> AgentResponseDto.success(result.content(), AgentType.SUPERVISOR));
    }

    public Mono<AgentResponseDto> resolveAlert(String name, String message) {
        log.info("Resolving alert: {}", name);
        return monitoringTools.resolveAlert(name, message)
                .map(result -> AgentResponseDto.success(result.content(), AgentType.SUPERVISOR));
    }
}
