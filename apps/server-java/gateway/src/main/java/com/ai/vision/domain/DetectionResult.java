package com.ai.vision.domain;

import java.util.List;

/**
 * Immutable result of object detection task.
 */
public record DetectionResult(
    List<DetectedObject> objects,
    float averageConfidence
) {
    public record DetectedObject(
        String label,
        float confidence,
        BoundingBox bbox
    ) {
        public DetectedObject {
            if (label == null || label.isBlank()) {
                throw new IllegalArgumentException("Label cannot be null or blank");
            }
            if (confidence < 0 || confidence > 1) {
                throw new IllegalArgumentException("Confidence must be between 0 and 1");
            }
        }
    }

    public record BoundingBox(
        float x,
        float y,
        float width,
        float height
    ) {
        public BoundingBox {
            if (width < 0 || height < 0) {
                throw new IllegalArgumentException("Width and height must be non-negative");
            }
        }
    }

    public static DetectionResult empty() {
        return new DetectionResult(List.of(), 0.0f);
    }

    public static DetectionResult of(List<DetectedObject> objects) {
        if (objects == null || objects.isEmpty()) {
            return empty();
        }
        float avgConf = (float) objects.stream()
            .mapToDouble(DetectedObject::confidence)
            .average()
            .orElse(0.0);
        return new DetectionResult(List.copyOf(objects), avgConf);
    }
}
