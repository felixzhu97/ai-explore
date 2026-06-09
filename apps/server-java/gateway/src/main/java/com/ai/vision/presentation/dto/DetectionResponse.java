package com.ai.vision.presentation.dto;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.util.List;

/**
 * Response DTO for object detection.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public record DetectionResponse(
    List<DetectedObject> objects
) {
    public record DetectedObject(
        String label,
        float confidence,
        BoundingBox bbox
    ) {}

    public record BoundingBox(
        float x,
        float y,
        float width,
        float height
    ) {}
}
