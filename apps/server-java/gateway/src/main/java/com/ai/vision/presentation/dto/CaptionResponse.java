package com.ai.vision.presentation.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record CaptionResponse(
    String task,
    String model,
    String caption,
    @JsonProperty("processing_time_ms") double processingTimeMs
) {
    public static CaptionResponse of(String model, String caption, double processingTimeMs) {
        return new CaptionResponse("caption_image", model, caption, processingTimeMs);
    }
}
