package com.ai.agents.infrastructure.tools;

import com.ai.agents.domain.ExecutionContext;
import com.ai.agents.domain.ToolCall;
import com.ai.agents.domain.ToolResult;
import com.ai.agents.domain.service.agents.K8sAgentService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Kubernetes tools implementation.
 * Provides kubectl-based operations for Kubernetes cluster management.
 */
@Component
public class K8sTools {

    private static final Logger log = LoggerFactory.getLogger(K8sTools.class);
    private final K8sAgentService domainService;

    public K8sTools(K8sAgentService domainService) {
        this.domainService = domainService;
    }

    public Mono<ToolResult> listPods(String namespace) {
        return executeTool("k8s_list_pods", Map.of("namespace", namespace), () -> {
            K8sAgentService.ValidationResult validation = domainService.validateCommand("get pods");
            if (!validation.isValid()) {
                return ToolResult.error(validation.error());
            }

            List<String> args = domainService.buildArgs("get", "pods", null, namespace);
            String output = runKubectl(args);
            return ToolResult.success(output);
        });
    }

    public Mono<ToolResult> getPod(String podName, String namespace) {
        return executeTool("k8s_get_pod", Map.of("podName", podName, "namespace", namespace), () -> {
            List<String> args = domainService.buildArgs("get", "pod", podName, namespace);
            args.add("-o");
            args.add("yaml");
            String output = runKubectl(args);
            return ToolResult.success(output);
        });
    }

    public Mono<ToolResult> describePod(String podName, String namespace) {
        return executeTool("k8s_describe_pod", Map.of("podName", podName, "namespace", namespace), () -> {
            List<String> args = domainService.buildArgs("describe", "pod", podName, namespace);
            String output = runKubectl(args);
            return ToolResult.success(output);
        });
    }

    public Mono<ToolResult> getPodLogs(String podName, String namespace, int lines) {
        return executeTool("k8s_get_pod_logs", Map.of("podName", podName, "namespace", namespace, "lines", lines), () -> {
            List<String> args = List.of("logs", podName, "-n", namespace, "--tail=" + lines);
            String output = runKubectl(args);
            return ToolResult.success(output);
        });
    }

    public Mono<ToolResult> listServices(String namespace) {
        return executeTool("k8s_list_services", Map.of("namespace", namespace), () -> {
            List<String> args = domainService.buildArgs("get", "services", null, namespace);
            String output = runKubectl(args);
            return ToolResult.success(output);
        });
    }

    public Mono<ToolResult> listDeployments(String namespace) {
        return executeTool("k8s_list_deployments", Map.of("namespace", namespace), () -> {
            List<String> args = domainService.buildArgs("get", "deployments", null, namespace);
            String output = runKubectl(args);
            return ToolResult.success(output);
        });
    }

    public Mono<ToolResult> scaleDeployment(String deploymentName, int replicas, String namespace) {
        return executeTool("k8s_scale_deployment", Map.of("deployment", deploymentName, "replicas", replicas, "namespace", namespace), () -> {
            if (replicas < 0 || replicas > 100) {
                return ToolResult.error("Replicas must be between 0 and 100");
            }
            List<String> args = List.of("scale", "deployment", deploymentName, "-n", namespace, "--replicas=" + replicas);
            String output = runKubectl(args);
            return ToolResult.success(output);
        });
    }

    public Mono<ToolResult> getNodeStatus() {
        return executeTool("k8s_get_node_status", Map.of(), () -> {
            List<String> args = List.of("get", "nodes", "-o", "wide");
            String output = runKubectl(args);
            return ToolResult.success(output);
        });
    }

    public Mono<ToolResult> getEvents(String namespace) {
        return executeTool("k8s_get_events", Map.of("namespace", namespace), () -> {
            List<String> args = List.of("get", "events", "-n", namespace, "--sort-by=.lastTimestamp");
            String output = runKubectl(args);
            return ToolResult.success(output);
        });
    }

    private String runKubectl(List<String> args) {
        try {
            ProcessBuilder pb = new ProcessBuilder();
            pb.command(new java.util.ArrayList<String>() {{
                add("kubectl");
                addAll(args);
            }});
            pb.redirectErrorStream(true);
            Process process = pb.start();

            String output = new String(process.getInputStream().readAllBytes());
            int exitCode = process.waitFor();

            if (exitCode != 0 && output.isEmpty()) {
                return "Command failed with exit code: " + exitCode;
            }
            return output.isEmpty() ? "Command executed successfully" : output;
        } catch (Exception e) {
            log.error("Error running kubectl: {}", e.getMessage());
            return "Error: " + e.getMessage();
        }
    }

    private <T> Mono<ToolResult> executeTool(String toolName, Map<String, Object> params, ToolExecutor executor) {
        long start = System.currentTimeMillis();
        try {
            ToolResult result = executor.execute();
            long duration = System.currentTimeMillis() - start;
            log.debug("Tool {} executed in {}ms", toolName, duration);
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
