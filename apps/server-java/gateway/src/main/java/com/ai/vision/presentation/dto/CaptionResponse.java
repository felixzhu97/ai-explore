package com.ai.vision.presentation.dto;

import com.fasterxml.jackson.annotation.JsonInclude;

/**
 * Response DTO for image captioning.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public record CaptionResponse(
    String caption
) {}
