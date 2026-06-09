package com.ai.vision.domain;

/**
 * Parameters for image generation task.
 */
public record GenerateParams(
    String prompt,
    String negativePrompt,
    int width,
    int height,
    int steps,
    float guidanceScale,
    String language
) {
    public GenerateParams {
        if (prompt == null || prompt.isBlank()) {
            throw new IllegalArgumentException("Prompt cannot be null or blank");
        }
        if (negativePrompt == null) {
            negativePrompt = "";
        }
        if (width <= 0) {
            width = 512;
        }
        if (height <= 0) {
            height = 512;
        }
        if (steps <= 0) {
            steps = 30;
        }
        if (guidanceScale <= 0) {
            guidanceScale = 7.5f;
        }
        if (language == null) {
            language = "eng";
        }
    }

    public static GenerateParams fromPrompt(String prompt) {
        return new GenerateParams(prompt, "", 512, 512, 30, 7.5f, "eng");
    }

    public GeneratedImage.GenerationMetadata toMetadata() {
        return new GeneratedImage.GenerationMetadata(
            prompt, negativePrompt, width, height, steps, guidanceScale
        );
    }
}
