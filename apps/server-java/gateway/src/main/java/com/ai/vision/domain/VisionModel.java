package com.ai.vision.domain;

import reactor.core.publisher.Mono;

/**
 * Port interface for vision model providers.
 * 
 * Located in Domain layer to ensure zero Spring dependency.
 * Concrete implementations are in Infrastructure layer.
 * 
 * This follows the Hexagonal Architecture (Ports and Adapters) pattern:
 * - Domain defines the port interface
 * - Infrastructure provides the adapter implementations
 */
public interface VisionModel {

    /**
     * Get the model type identifier.
     */
    ModelType type();

    /**
     * Check if the model is loaded and ready for inference.
     */
    boolean isAvailable();

    /**
     * Object detection using YOLO.
     * 
     * @param imageData The input image data
     * @param confidence Confidence threshold (0.0-1.0)
     * @return Mono emitting detection result
     */
    default Mono<DetectionResult> detect(ImageData imageData, float confidence) {
        return Mono.error(new UnsupportedOperationException(
            "detect not supported by " + type()));
    }

    /**
     * Image captioning using BLIP.
     * 
     * @param imageData The input image data
     * @return Mono emitting caption result
     */
    default Mono<CaptionResult> caption(ImageData imageData) {
        return Mono.error(new UnsupportedOperationException(
            "caption not supported by " + type()));
    }

    /**
     * OCR text recognition.
     * 
     * @param imageData The input image data
     * @param language Language code (e.g., "eng", "chi_sim")
     * @return Mono emitting OCR result
     */
    default Mono<OcrResult> recognizeText(ImageData imageData, String language) {
        return Mono.error(new UnsupportedOperationException(
            "recognizeText not supported by " + type()));
    }

    /**
     * Image generation using Stable Diffusion.
     * 
     * @param params Generation parameters including prompt
     * @return Mono emitting generated image result
     */
    default Mono<GeneratedImage> generate(GenerateParams params) {
        return Mono.error(new UnsupportedOperationException(
            "generate not supported by " + type()));
    }
}
