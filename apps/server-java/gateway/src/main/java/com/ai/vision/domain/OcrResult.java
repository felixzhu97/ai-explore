package com.ai.vision.domain;

import java.util.List;

/**
 * Immutable result of OCR text recognition task.
 */
public record OcrResult(
    String text,
    float confidence,
    List<TextBlock> blocks
) {
    public record TextBlock(
        String text,
        float confidence,
        BoundingBox bbox
    ) {
        public TextBlock {
            if (text == null) {
                throw new IllegalArgumentException("Text block text cannot be null");
            }
        }
    }

    public record BoundingBox(
        float x1,
        float y1,
        float x2,
        float y2
    ) {}

    public OcrResult {
        if (blocks == null) {
            blocks = List.of();
        }
    }

    public static OcrResult of(String text) {
        return new OcrResult(text, 0.0f, List.of());
    }

    public static OcrResult of(String text, float confidence, List<TextBlock> blocks) {
        return new OcrResult(text, confidence, List.copyOf(blocks));
    }

    public boolean hasText() {
        return text != null && !text.isBlank();
    }
}
