package com.ai.agents.application.service;

import com.ai.agents.domain.*;
import com.ai.agents.domain.service.agents.K8sAgentService;
import com.ai.agents.infrastructure.tools.K8sTools;
import com.ai.agents.presentation.dto.AgentResponseDto;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.util.List;

/**
 * Use case for K8s agent operations.
 */
@Service
public class K8sUseCase {

    private static final Logger log = LoggerFactory.getLogger(K8sUseCase.class);

    private final K8sAgentService domainService;
    private final K8sTools k8sTools;

    public K8sUseCase(K8sAgentService domainService, K8sTools k8sTools) {
        this.domainService = domainService;
        this.k8sTools = k8sTools;
    }

    public Mono<AgentResponseDto> listPods(String namespace) {
        log.info("Listing pods in namespace: {}", namespace);
        return k8sTools.listPods(namespace)
                .map(result -> AgentResponseDto.success(result.content(), AgentType.SUPERVISOR));
    }

    public Mono<AgentResponseDto> getPod(String podName, String namespace) {
        log.info("Getting pod: {} in namespace: {}", podName, namespace);
        return k8sTools.getPod(podName, namespace)
                .map(result -> AgentResponseDto.success(result.content(), AgentType.SUPERVISOR));
    }

    public Mono<AgentResponseDto> describePod(String podName, String namespace) {
        log.info("Describing pod: {} in namespace: {}", podName, namespace);
        return k8sTools.describePod(podName, namespace)
                .map(result -> AgentResponseDto.success(result.content(), AgentType.SUPERVISOR));
    }

    public Mono<AgentResponseDto> getPodLogs(String podName, String namespace, int lines) {
        log.info("Getting logs for pod: {} in namespace: {}", podName, namespace);
        return k8sTools.getPodLogs(podName, namespace, lines)
                .map(result -> AgentResponseDto.success(result.content(), AgentType.SUPERVISOR));
    }

    public Mono<AgentResponseDto> listServices(String namespace) {
        log.info("Listing services in namespace: {}", namespace);
        return k8sTools.listServices(namespace)
                .map(result -> AgentResponseDto.success(result.content(), AgentType.SUPERVISOR));
    }

    public Mono<AgentResponseDto> listDeployments(String namespace) {
        log.info("Listing deployments in namespace: {}", namespace);
        return k8sTools.listDeployments(namespace)
                .map(result -> AgentResponseDto.success(result.content(), AgentType.SUPERVISOR));
    }

    public Mono<AgentResponseDto> scaleDeployment(String deploymentName, int replicas, String namespace) {
        log.info("Scaling deployment: {} to {} replicas", deploymentName, replicas);

        K8sAgentService.ValidationResult validation = domainService.validateCommand("scale");
        if (!validation.isValid()) {
            return Mono.just(AgentResponseDto.error(validation.error()));
        }

        if (replicas < 0 || replicas > 100) {
            return Mono.just(AgentResponseDto.error("Replicas must be between 0 and 100"));
        }

        return k8sTools.scaleDeployment(deploymentName, replicas, namespace)
                .map(result -> AgentResponseDto.success(result.content(), AgentType.SUPERVISOR));
    }

    public Mono<AgentResponseDto> getNodeStatus() {
        log.info("Getting node status");
        return k8sTools.getNodeStatus()
                .map(result -> AgentResponseDto.success(result.content(), AgentType.SUPERVISOR));
    }

    public Mono<AgentResponseDto> getEvents(String namespace) {
        log.info("Getting events in namespace: {}", namespace);
        return k8sTools.getEvents(namespace)
                .map(result -> AgentResponseDto.success(result.content(), AgentType.SUPERVISOR));
    }

    public Mono<AgentResponseDto> executeKubectl(String command) {
        log.info("Executing kubectl command: {}", command);

        K8sAgentService.ValidationResult validation = domainService.validateCommand(command);
        if (!validation.isValid()) {
            return Mono.just(AgentResponseDto.error(validation.error()));
        }

        List<String> args = List.of(command.split("\\s+"));
        return Mono.just(AgentResponseDto.success("Command validated: " + command, AgentType.SUPERVISOR));
    }
}
