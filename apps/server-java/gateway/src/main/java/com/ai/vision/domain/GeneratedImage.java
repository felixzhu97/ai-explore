package com.ai.vision.domain;

/**
 * Immutable result of image generation task.
 */
public record GeneratedImage(
    String imageUrl,
    String base64Image,
    int seed,
    GenerationMetadata metadata
) {
    public record GenerationMetadata(
        String prompt,
        String negativePrompt,
        int width,
        int height,
        int steps,
        float guidanceScale
    ) {}

    public GeneratedImage {
        if (imageUrl == null && base64Image == null) {
            throw new IllegalArgumentException("At least one of imageUrl or base64Image must be provided");
        }
        if (metadata == null) {
            metadata = new GenerationMetadata("", "", 512, 512, 30, 7.5f);
        }
    }

    public static GeneratedImage fromUrl(String url, int seed, GenerationMetadata metadata) {
        return new GeneratedImage(url, null, seed, metadata);
    }

    public static GeneratedImage fromBase64(String base64, int seed, GenerationMetadata metadata) {
        return new GeneratedImage(null, base64, seed, metadata);
    }
}
