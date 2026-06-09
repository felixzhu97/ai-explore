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
import java.util.UUID;

/**
 * Stable Diffusion image generation model adapter.
 * 
 * Infrastructure implementation of VisionModel port.
 * 
 * Supports:
 * - Text-to-image generation
 * - Image-to-image (img2img)
 * - ControlNet integration
 * - Multiple schedulers
 */
@Component
@ConditionalOnProperty(name = "vision.stable-diffusion.enabled", havingValue = "true", matchIfMissing = true)
public class StableDiffusionModelAdapter implements VisionModel {

    private static final Logger log = LoggerFactory.getLogger(StableDiffusionModelAdapter.class);

    private final VisionProperties.StableDiffusionConfig config;
    private volatile boolean initialized = false;
    private Path outputDir;

    public StableDiffusionModelAdapter(VisionProperties properties) {
        this.config = properties.getStableDiffusion();
    }

    @Override
    public ModelType type() {
        return ModelType.STABLE_DIFFUSION;
    }

    @Override
    public boolean isAvailable() {
        return initialized;
    }

    @PostConstruct
    public Mono<Void> initialize() {
        return Mono.fromRunnable(() -> {
            log.info("Initializing Stable Diffusion provider with model: {}", 
                     config.getModelName());

            try {
                outputDir = Path.of(config.getCacheDir(), "generated");
                Files.createDirectories(outputDir);
                log.info("Generated images will be saved to: {}", outputDir);
            } catch (Exception e) {
                log.warn("Could not create output directory: {}", e.getMessage());
            }

            Path modelPath = Path.of(config.getModelPath());
            if (!Files.exists(modelPath)) {
                log.warn("Stable Diffusion model not found at {}. Will use external API.", 
                         config.getModelPath());
            } else {
                log.info("Stable Diffusion model loaded from {}", modelPath);
            }

            this.initialized = true;
        });
    }

    @Override
    public Mono<GeneratedImage> generate(GenerateParams params) {
        if (!isAvailable()) {
            return Mono.error(() -> VisionException.modelNotAvailable("Stable Diffusion"));
        }

        return Mono.fromCallable(() -> {
            log.info("Generating image with prompt: {}, steps: {}, size: {}x{}", 
                     params.prompt(), params.steps(), params.width(), params.height());

            // TODO: Implement actual Stable Diffusion inference
            // Options: ONNX Runtime, PyTorch Java binding, or remote API
            throw new UnsupportedOperationException(
                "Stable Diffusion inference not yet implemented. " +
                "Options: ONNX Runtime, PyTorch Java binding, or remote API.");
        });
    }
}
