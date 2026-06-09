package com.ai.vision.presentation.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record VariationRequest(
    String image,
    @Size(min = 1, max = 4000) String prompt,
    float strength,
    @JsonProperty("num_inference_steps") @Min(1) @Max(150) int numInferenceSteps,
    @JsonProperty("guidance_scale") @Min(1) @Max(20) float guidanceScale,
    Integer seed,
    @JsonProperty("num_images") @Min(1) @Max(4) int numImages
) {
    public VariationRequest {
        if (strength == 0) strength = 0.5f;
        if (numInferenceSteps == 0) numInferenceSteps = 30;
        if (guidanceScale == 0) guidanceScale = 7.5f;
        if (numImages == 0) numImages = 1;
    }
}
