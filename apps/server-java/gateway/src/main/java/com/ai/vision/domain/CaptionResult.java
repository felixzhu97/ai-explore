package com.ai.vision.domain;

/**
 * Immutable result of image captioning task.
 */
public record CaptionResult(
    String caption,
    float confidence
) {
    public CaptionResult {
        if (caption == null || caption.isBlank()) {
            throw new IllegalArgumentException("Caption cannot be null or blank");
        }
        if (confidence < 0 || confidence > 1) {
            throw new IllegalArgumentException("Confidence must be between 0 and 1");
        }
    }

    public static CaptionResult of(String caption) {
        return new CaptionResult(caption, 1.0f);
    }

    public static CaptionResult of(String caption, float confidence) {
        return new CaptionResult(caption, confidence);
    }
}
