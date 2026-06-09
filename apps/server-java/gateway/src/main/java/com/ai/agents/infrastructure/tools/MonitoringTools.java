package com.ai.agents.infrastructure.tools;

import com.ai.agents.domain.ToolResult;
import com.ai.agents.domain.service.agents.MonitoringAgentService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.util.List;

/**
 * Monitoring tools implementation.
 * Provides Prometheus/Grafana metrics and alerting tools.
 */
@Component
public class MonitoringTools {

    private static final Logger log = LoggerFactory.getLogger(MonitoringTools.class);
    private final MonitoringAgentService domainService;

    public MonitoringTools(MonitoringAgentService domainService) {
        this.domainService = domainService;
    }

    public Mono<ToolResult> queryMetrics(String metric, String timeRange, String aggregation) {
        return executeTool("monitoring_query_metrics", () -> {
            MonitoringAgentService.MetricQueryResult result = domainService.queryMetrics(metric, timeRange, aggregation);

            String output = String.format("""
                    Metric Query Result

                    Metric: %s
                    Time Range: %s
                    Aggregation: %s

                    Current Value: %.2f
                    Average: %.2f
                    Min: %.2f
                    Max: %.2f
                    Timestamp: %s""",
                    result.metric(),
                    result.timeRange(),
                    result.aggregation(),
                    result.value(),
                    result.avg(),
                    result.min(),
                    result.max(),
                    result.timestamp()
            );
            return ToolResult.success(output);
        });
    }

    public Mono<ToolResult> createAlert(String name, String metric, String condition, double threshold) {
        return executeTool("monitoring_create_alert", () -> {
            MonitoringAgentService.AlertRule rule = domainService.createAlert(name, metric, condition, threshold);

            String output = String.format("""
                    Alert Rule Created

                    Name: %s
                    Metric: %s
                    Condition: %s %s %.2f
                    Status: %s
                    Created: %s""",
                    rule.name(),
                    rule.metric(),
                    rule.metric(),
                    rule.condition(),
                    rule.threshold(),
                    rule.status(),
                    rule.createdAt()
            );
            return ToolResult.success(output);
        });
    }

    public Mono<ToolResult> listAlerts(String status) {
        return executeTool("monitoring_list_alerts", () -> {
            List<MonitoringAgentService.AlertRule> alerts = domainService.listAlerts(status);

            if (alerts.isEmpty()) {
                return ToolResult.success("No alerts found.");
            }

            StringBuilder output = new StringBuilder("Alert Rules:\n\n");
            for (MonitoringAgentService.AlertRule alert : alerts) {
                output.append("- ").append(alert.name()).append(": ").append(alert.metric())
                        .append(" ").append(alert.condition()).append(" ").append(alert.threshold())
                        .append(" [").append(alert.status()).append("]\n");
            }
            return ToolResult.success(output.toString());
        });
    }

    public Mono<ToolResult> fireAlert(String name, String message) {
        return executeTool("monitoring_fire_alert", () -> {
            MonitoringAgentService.AlertRule rule = domainService.fireAlert(name, message);

            String output = String.format("""
                    Alert Fired!

                    Name: %s
                    Metric: %s
                    Status: %s
                    Message: %s""",
                    rule.name(),
                    rule.metric(),
                    rule.status(),
                    message
            );
            return ToolResult.success(output);
        });
    }

    public Mono<ToolResult> resolveAlert(String name, String message) {
        return executeTool("monitoring_resolve_alert", () -> {
            MonitoringAgentService.AlertRule rule = domainService.resolveAlert(name, message);

            String output = String.format("""
                    Alert Resolved

                    Name: %s
                    Metric: %s
                    Status: %s
                    Resolution: %s""",
                    rule.name(),
                    rule.metric(),
                    rule.status(),
                    message
            );
            return ToolResult.success(output);
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
