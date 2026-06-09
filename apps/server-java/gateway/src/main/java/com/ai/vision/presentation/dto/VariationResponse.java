package com.ai.vision.presentation.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record VariationResponse(
    List<String> images,
    int seed,
    String prompt,
    float strength,
    @JsonProperty("inference_steps") int inferenceSteps,
    @JsonProperty("processing_time_ms") double processingTimeMs
) {}
