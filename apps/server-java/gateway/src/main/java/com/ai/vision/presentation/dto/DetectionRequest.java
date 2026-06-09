package com.ai.vision.presentation.dto;

import com.fasterxml.jackson.annotation.JsonInclude;

/**
 * Request DTO for object detection.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public record DetectionRequest(
    float confidence
) {
    public DetectionRequest {
        if (confidence <= 0) confidence = 0.5f;
        if (confidence > 1) confidence = 1.0f;
    }

    public static DetectionRequest defaultConfig() {
        return new DetectionRequest(0.5f);
    }
}
