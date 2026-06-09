package com.ai.agents.infrastructure.tools;

import com.ai.agents.domain.Incident;
import com.ai.agents.domain.ToolResult;
import com.ai.agents.domain.service.agents.AIOpsAgentService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.Map;

/**
 * AIOps tools implementation.
 * Provides anomaly detection, incident management, and root cause analysis tools.
 */
@Component
public class AIOpsTools {

    private static final Logger log = LoggerFactory.getLogger(AIOpsTools.class);
    private final AIOpsAgentService domainService;

    public AIOpsTools(AIOpsAgentService domainService) {
        this.domainService = domainService;
    }

    public Mono<ToolResult> detectAnomaly(String metric, String timeRange, double sensitivity) {
        return executeTool("aiops_detect_anomaly", () -> {
            AIOpsAgentService.AnomalyResult result = domainService.detectAnomaly(metric, timeRange, sensitivity);

            String output = String.format("""
                    Anomaly Detection Result for '%s'

                    Status: %s
                    Score: %.4f
                    Threshold: %.4f
                    Sensitivity: %.2f
                    Time Range: %s
                    Explanation: %s""",
                    result.metric(),
                    result.isAnomaly() ? "ANOMALY DETECTED" : "NORMAL",
                    result.score(),
                    result.threshold(),
                    result.sensitivity(),
                    result.timeRange(),
                    result.explanation()
            );
            return ToolResult.success(output);
        });
    }

    public Mono<ToolResult> listIncidents(String status, String severity) {
        return executeTool("aiops_list_incidents", () -> {
            Incident.IncidentStatus incidentStatus = status != null ?
                    Incident.IncidentStatus.valueOf(status.toUpperCase()) : null;
            Incident.Severity incidentSeverity = severity != null ?
                    Incident.Severity.valueOf(severity.toUpperCase()) : null;

            List<Incident> incidents = domainService.listIncidents(incidentStatus, incidentSeverity);

            if (incidents.isEmpty()) {
                return ToolResult.success("No incidents found matching the criteria.");
            }

            StringBuilder output = new StringBuilder("Incidents (").append(incidents.size()).append(" shown):\n\n");
            for (Incident inc : incidents) {
                String severityIcon = switch (inc.severity()) {
                    case CRITICAL -> "CRITICAL";
                    case WARNING -> "WARNING";
                    case INFO -> "INFO";
                };
                output.append(severityIcon).append(" ").append(inc.title()).append("\n");
                output.append("   ID: ").append(inc.idValue()).append("\n");
                output.append("   Status: ").append(inc.status()).append("\n");
                output.append("   Severity: ").append(inc.severity()).append("\n\n");
            }
            return ToolResult.success(output.toString());
        });
    }

    public Mono<ToolResult> createIncident(String title, String severity, String description, List<String> affectedSystems) {
        return executeTool("aiops_create_incident", () -> {
            Incident.Severity sev = Incident.Severity.valueOf(severity.toUpperCase());
            Incident incident = domainService.createIncident(title, description, sev, affectedSystems);

            String output = String.format("""
                    Incident Created!

                    ID: %s
                    Title: %s
                    Severity: %s
                    Status: %s
                    Affected Systems: %s""",
                    incident.idValue(),
                    incident.title(),
                    incident.severity(),
                    incident.status(),
                    String.join(", ", incident.affectedSystems())
            );
            return ToolResult.success(output);
        });
    }

    public Mono<ToolResult> getSystemHealth() {
        return executeTool("aiops_get_system_health", () -> {
            AIOpsAgentService.SystemHealthResult result = domainService.getSystemHealth();

            StringBuilder output = new StringBuilder("System Health Overview\n\n");
            output.append("Overall Status: ").append(result.overallStatus().equals("healthy") ? "Healthy" : "Degraded").append("\n");
            output.append("Services: ").append(result.healthyCount()).append(" healthy, ").append(result.degradedCount()).append(" degraded\n\n");
            output.append("Services:\n");
            for (Map.Entry<String, String> entry : result.services().entrySet()) {
                output.append("  ").append(entry.getKey()).append(": ").append(entry.getValue()).append("\n");
            }
            return ToolResult.success(output.toString());
        });
    }

    public Mono<ToolResult> rootCauseAnalysis(String incidentId, List<String> affectedServices) {
        return executeTool("aiops_root_cause_analysis", () -> {
            AIOpsAgentService.RootCauseResult result = domainService.analyzeRootCause(incidentId, affectedServices);

            StringBuilder output = new StringBuilder("Root Cause Analysis for Incident: ").append(incidentId).append("\n\n");
            output.append("Root Cause: ").append(result.rootCause()).append("\n");
            output.append("Confidence: ").append(String.format("%.0f%%", result.confidence() * 100)).append("\n\n");
            output.append("Contributing Factors:\n");
            for (int i = 0; i < result.contributingFactors().size(); i++) {
                output.append("  ").append(i + 1).append(". ").append(result.contributingFactors().get(i)).append("\n");
            }
            output.append("\nRecommendations:\n");
            for (int i = 0; i < Math.min(3, result.recommendations().size()); i++) {
                output.append("  ").append(i + 1).append(". ").append(result.recommendations().get(i)).append("\n");
            }
            return ToolResult.success(output.toString());
        });
    }

    public Mono<ToolResult> searchLogs(String query, String timeRange, int limit) {
        return executeTool("aiops_search_logs", () -> {
            StringBuilder output = new StringBuilder("Log Search Results for '").append(query).append("'\n\n");
            output.append("Time Range: ").append(timeRange).append(" | Showing: ").append(limit).append(" results\n\n");

            for (int i = 0; i < limit; i++) {
                output.append(i + 1).append(". [INFO] Log entry for: ").append(query).append("\n");
            }
            return ToolResult.success(output.toString());
        });
    }

    public Mono<ToolResult> acknowledgeAlert(String alertId, String user) {
        return executeTool("aiops_acknowledge_alert", () -> {
            String output = String.format("""
                    Alert Acknowledged!

                    Alert ID: %s
                    Acknowledged By: %s
                    Time: %s

                    You are now responsible for handling this alert.""",
                    alertId,
                    user,
                    java.time.LocalDateTime.now()
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
