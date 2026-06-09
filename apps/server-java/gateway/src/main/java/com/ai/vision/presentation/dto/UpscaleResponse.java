package com.ai.vision.presentation.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record UpscaleResponse(
    String image,
    int scale,
    @JsonProperty("original_width") int originalWidth,
    @JsonProperty("original_height") int originalHeight,
    @JsonProperty("new_width") int newWidth,
    @JsonProperty("new_height") int newHeight,
    @JsonProperty("processing_time_ms") double processingTimeMs
) {}
