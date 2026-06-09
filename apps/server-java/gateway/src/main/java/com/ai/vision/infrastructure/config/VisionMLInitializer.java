package com.ai.vision.infrastructure.config;

import com.ai.vision.domain.VisionModel;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.util.List;

/**
 * Initializer for Vision ML providers.
 * 
 * This component ensures all VisionModel providers are properly initialized
 * at application startup by subscribing to their initialization Mono streams.
 * 
 * Note: @PostConstruct methods returning Mono<Void> are NOT automatically
 * subscribed to by Spring. This initializer explicitly subscribes to
 * ensure providers are marked as initialized.
 */
@Component
public class VisionMLInitializer {

    private static final Logger log = LoggerFactory.getLogger(VisionMLInitializer.class);

    private final List<VisionModel> visionModels;

    public VisionMLInitializer(List<VisionModel> visionModels) {
        this.visionModels = visionModels;
    }

    @EventListener(ContextRefreshedEvent.class)
    public void initializeModels() {
        log.info("Starting ML provider initialization for {} model(s)", visionModels.size());
        
        for (VisionModel model : visionModels) {
            String modelType = model.type().name();
            log.info("Initializing provider: {}", modelType);
            
            try {
                Mono<Void> initMono = model.initialize();
                if (initMono != null) {
                    initMono
                        .publishOn(Schedulers.boundedElastic())
                        .doOnSuccess(v -> log.info("Provider {} initialized successfully", modelType))
                        .doOnError(e -> log.error("Provider {} initialization failed: {}", 
                            modelType, e.getMessage(), e))
                        .onErrorResume(e -> {
                            log.warn("Provider {} failed to initialize, marking as unavailable: {}", 
                                modelType, e.getMessage());
                            return Mono.empty();
                        })
                        .subscribe();
                } else {
                    log.warn("Provider {} returned null from initialize(), skipping", modelType);
                }
            } catch (Exception e) {
                log.error("Failed to start initialization for provider {}: {}", 
                    modelType, e.getMessage(), e);
            }
        }
        
        log.info("ML provider initialization started");
    }

    @PostConstruct
    public void postConstruct() {
        log.info("VisionMLInitializer constructed with {} providers: {}", 
            visionModels.size(),
            visionModels.stream()
                .map(m -> m.type().name())
                .toList());
    }
}
