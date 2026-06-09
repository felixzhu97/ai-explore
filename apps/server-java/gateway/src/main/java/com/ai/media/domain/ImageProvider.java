package com.ai.media.domain;

import reactor.core.publisher.Mono;

/**
 * Port interface for image generation providers.
 * Defines the contract for any image generation backend implementation.
 * Located in Domain layer following Clean Architecture principles.
 */
public interface ImageProvider {

    /**
     * Generate images from text prompt.
     *
     * @param params generation parameters
     * @return generated image wrapped in Mono
     */
    Mono<GeneratedImage> generate(GenerationParams params);

    /**
     * List available models.
     *
     * @return list of available models
     */
    Mono<java.util.List<ModelInfo>> listModels();

    /**
     * List available LoRA models.
     *
     * @return list of available LoRAs
     */
    Mono<java.util.List<LoraInfo>> listLoras();

    /**
     * Check if the provider is healthy and ready.
     *
     * @return true if ready to serve requests
     */
    Mono<Boolean> isHealthy();
}
