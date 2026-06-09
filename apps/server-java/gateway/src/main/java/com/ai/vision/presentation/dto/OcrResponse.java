package com.ai.vision.presentation.dto;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.util.List;

/**
 * Response DTO for OCR text recognition.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public record OcrResponse(
    String text,
    float confidence,
    List<TextBlock> blocks
) {
    public record TextBlock(
        String text,
        float confidence,
        BoundingBox bbox
    ) {}

    public record BoundingBox(
        float x1,
        float y1,
        float x2,
        float y2
    ) {}

    public OcrResponse {
        if (blocks == null) blocks = List.of();
    }
}
