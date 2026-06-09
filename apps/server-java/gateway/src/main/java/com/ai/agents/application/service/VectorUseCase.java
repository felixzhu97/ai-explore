package com.ai.agents.application.service;

import com.ai.agents.domain.*;
import com.ai.agents.domain.service.agents.VectorAgentService;
import com.ai.agents.infrastructure.tools.VectorTools;
import com.ai.agents.presentation.dto.AgentResponseDto;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.util.List;

/**
 * Use case for Vector DB agent operations.
 */
@Service
public class VectorUseCase {

    private static final Logger log = LoggerFactory.getLogger(VectorUseCase.class);

    private final VectorAgentService domainService;
    private final VectorTools vectorTools;

    public VectorUseCase(VectorAgentService domainService, VectorTools vectorTools) {
        this.domainService = domainService;
        this.vectorTools = vectorTools;
    }

    public Mono<AgentResponseDto> createCollection(String name, int dimension, String description) {
        log.info("Creating collection: {}", name);
        return vectorTools.createCollection(name, dimension, description)
                .map(result -> AgentResponseDto.success(result.content(), AgentType.SUPERVISOR));
    }

    public Mono<AgentResponseDto> listCollections() {
        log.info("Listing collections");
        return vectorTools.listCollections()
                .map(result -> AgentResponseDto.success(result.content(), AgentType.SUPERVISOR));
    }

    public Mono<AgentResponseDto> query(String collectionName, List<Float> embedding, int topK) {
        log.info("Querying collection: {}", collectionName);
        return vectorTools.query(collectionName, embedding, topK)
                .map(result -> AgentResponseDto.success(result.content(), AgentType.SUPERVISOR));
    }

    public Mono<AgentResponseDto> addVectors(String collectionName, List<String> ids, List<String> documents) {
        log.info("Adding vectors to collection: {}", collectionName);
        return vectorTools.addVectors(collectionName, ids, documents)
                .map(result -> AgentResponseDto.success(result.content(), AgentType.SUPERVISOR));
    }
}
