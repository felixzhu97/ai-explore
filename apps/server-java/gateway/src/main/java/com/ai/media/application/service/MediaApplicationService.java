package com.ai.media.application.service;

import com.ai.media.domain.*;
import com.ai.media.domain.exception.MediaException;
import com.ai.media.infrastructure.config.MediaProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.util.List;

/**
 * Application service coordinating media generation operations.
 * Orchestrates domain objects and infrastructure adapters.
 */
@Service
public class MediaApplicationService {

    private static final Logger log = LoggerFactory.getLogger(MediaApplicationService.class);

    private final ImageProvider imageProvider;
    private final MediaProperties properties;

    public MediaApplicationService(ImageProvider imageProvider, MediaProperties properties) {
        this.imageProvider = imageProvider;
        this.properties = properties;
    }

    /**
     * Generate images from text prompt.
     */
    public Mono<ImageGenerationResult> generate(GenerationParams params) {
        log.info("Processing image generation request: prompt='{}...'", 
                truncatePrompt(params.prompt()));

        return Mono.just(GenerationTask.create(params))
                .flatMap(task -> task.generate(imageProvider)
                        .map(image -> new ImageGenerationResult(
                                List.of(image.base64Data()),
                                image.seed(),
                                params.model() != null ? params.model() 
                                        : properties.stableDiffusion().defaultModel(),
                                params.prompt(),
                                params.width(),
                                params.height(),
                                params.steps(),
                                params.cfgScale(),
                                task.processingTimeMs()
                        ))
                        .doOnSuccess(result -> log.info("Image generated successfully in {}ms", 
                                result.processingTimeMs()))
                        .doOnError(error -> log.error("Image generation failed: {}", error.getMessage(), error))
                )
                .onErrorResume(error -> {
                    log.error("Generation failed: {}", error.getMessage());
                    return Mono.just(ImageGenerationResult.failure(error.getMessage()));
                });
    }

    /**
     * List available models.
     */
    public Mono<List<ModelInfo>> listModels() {
        return imageProvider.listModels()
                .doOnSuccess(models -> log.debug("Listed {} models", models.size()))
                .onErrorResume(error -> {
                    log.warn("Failed to list models, returning defaults: {}", error.getMessage());
                    return Mono.just(getDefaultModels());
                });
    }

    /**
     * Get a specific model by ID.
     */
    public Mono<ModelInfo> getModel(String modelId) {
        return imageProvider.listModels()
                .map(models -> models.stream()
                        .filter(m -> m.id().equals(modelId))
                        .findFirst()
                        .orElseThrow(() -> new MediaException("Model not found: " + modelId)))
                .onErrorResume(error -> {
                    return imageProvider.listModels()
                            .map(models -> models.stream()
                                    .filter(m -> m.id().equals(modelId))
                                    .findFirst()
                                    .orElseThrow(() -> new MediaException("Model not found: " + modelId)));
                });
    }

    /**
     * List available LoRAs.
     */
    public Mono<List<LoraInfo>> listLoras() {
        return imageProvider.listLoras()
                .doOnSuccess(loras -> log.debug("Listed {} LoRAs", loras.size()))
                .onErrorResume(error -> {
                    log.warn("Failed to list LoRAs: {}", error.getMessage());
                    return Mono.just(List.of());
                });
    }

    /**
     * Check service health.
     */
    public Mono<Boolean> isHealthy() {
        return imageProvider.isHealthy();
    }

    private List<ModelInfo> getDefaultModels() {
        return properties.stableDiffusion().availableModels().stream()
                .map(model -> ModelInfo.of(model, model, "stable-diffusion", 
                        model.equals(properties.stableDiffusion().defaultModel())))
                .toList();
    }

    private String truncatePrompt(String prompt) {
        if (prompt == null) return "";
        return prompt.substring(0, Math.min(50, prompt.length()));
    }

    /**
     * Result record for image generation operations.
     */
    public record ImageGenerationResult(
            List<String> images,
            long seed,
            String model,
            String prompt,
            int width,
            int height,
            int numInferenceSteps,
            float guidanceScale,
            double processingTimeMs,
            boolean success,
            String error
    ) {
        public ImageGenerationResult(
                List<String> images,
                long seed,
                String model,
                String prompt,
                int width,
                int height,
                int numInferenceSteps,
                float guidanceScale,
                double processingTimeMs
        ) {
            this(images, seed, model, prompt, width, height, numInferenceSteps, 
                    guidanceScale, processingTimeMs, true, null);
        }

        public static ImageGenerationResult failure(String error) {
            return new ImageGenerationResult(
                    List.of("data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAAAAEAAAABCAYAAAAfFcSJAAAADUlEQVR42mNk+M9QDwADhgGAWjR9awAAAABJRU5ErkJggg=="),
                    System.currentTimeMillis() % Integer.MAX_VALUE,
                    "error-model",
                    "error",
                    512, 512, 25, 7.5f,
                    0,
                    false,
                    error
            );
        }
    }
}
