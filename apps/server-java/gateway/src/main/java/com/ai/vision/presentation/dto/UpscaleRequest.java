package com.ai.vision.presentation.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record UpscaleRequest(
    String image,
    @Min(2) @Max(4) int scale,
    String prompt
) {
    public UpscaleRequest {
        if (scale == 0) scale = 2;
    }
}
