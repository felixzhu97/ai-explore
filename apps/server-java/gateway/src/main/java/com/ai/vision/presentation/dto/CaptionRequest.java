package com.ai.vision.presentation.dto;

import com.fasterxml.jackson.annotation.JsonInclude;

/**
 * Request DTO for image captioning.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public record CaptionRequest(
    String imageUrl,
    String description
) {}
