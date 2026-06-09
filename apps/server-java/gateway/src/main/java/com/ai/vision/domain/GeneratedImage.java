package com.ai.vision.domain;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Immutable result of image generation task.
 */
public record GeneratedImage(
    @JsonProperty("image_url") String imageUrl,
    @JsonProperty("base64_image") String base64Image,
    int seed,
    GenerationMetadata metadata
) {
    public record GenerationMetadata(
        String prompt,
        @JsonProperty("negative_prompt") String negativePrompt,
        int width,
        int height,
        int steps,
        @JsonProperty("guidance_scale") float guidanceScale
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
