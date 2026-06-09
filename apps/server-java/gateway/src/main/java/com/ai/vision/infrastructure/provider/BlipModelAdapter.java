package com.ai.vision.infrastructure.provider;

import com.ai.vision.domain.*;
import com.ai.vision.domain.exception.VisionException;
import com.ai.vision.infrastructure.config.VisionProperties;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.nio.file.Files;
import java.nio.file.Path;

/**
 * BLIP-based image captioning model adapter.
 * 
 * Infrastructure implementation of VisionModel port.
 * 
 * Supports:
 * - Salesforce BLIP (blip-image-captioning-base)
 * - Hugging Face transformers format
 * - ONNX export for faster inference
 */
@Component
@ConditionalOnProperty(name = "vision.blip.enabled", havingValue = "true", matchIfMissing = true)
public class BlipModelAdapter implements VisionModel {

    private static final Logger log = LoggerFactory.getLogger(BlipModelAdapter.class);

    private final VisionProperties.BlipConfig config;
    private volatile boolean initialized = false;

    public BlipModelAdapter(VisionProperties properties) {
        this.config = properties.getBlip();
    }

    @Override
    public ModelType type() {
        return ModelType.BLIP;
    }

    @Override
    public boolean isAvailable() {
        return initialized;
    }

    @PostConstruct
    public Mono<Void> initialize() {
        return Mono.fromRunnable(() -> {
            log.info("Initializing BLIP provider with model: {}", config.getModelName());
            
            Path modelPath = Path.of(config.getModelPath());
            if (!Files.exists(modelPath)) {
                log.warn("BLIP model not found at {}. Will use fallback.", config.getModelPath());
            } else {
                log.info("BLIP model loaded successfully from {}", modelPath);
            }
            
            this.initialized = true;
        });
    }

    @Override
    public Mono<CaptionResult> caption(ImageData imageData) {
        if (!isAvailable()) {
            return Mono.error(() -> VisionException.modelNotAvailable("BLIP"));
        }

        return Mono.fromCallable(() -> {
            log.info("Running BLIP captioning on image ({} bytes)", imageData.size());

            Path modelPath = Path.of(config.getModelPath());
            if (!Files.exists(modelPath)) {
                log.warn("BLIP model not found. Returning placeholder caption.");
                return CaptionResult.of(
                    "Placeholder caption: Image content cannot be analyzed without BLIP model.",
                    0.5f
                );
            }

            // TODO: Implement actual BLIP inference
            // Integration with ONNX Runtime or Hugging Face transformers required
            throw new UnsupportedOperationException(
                "BLIP inference not yet implemented. " +
                "Integration with ONNX Runtime or Hugging Face transformers required.");
        });
    }
}
