package com.ai.vision.presentation.dto;

import com.fasterxml.jackson.annotation.JsonInclude;

/**
 * Response DTO for image generation.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public record GenerateResponse(
    String imageUrl,
    String base64Image,
    int seed
) {}
