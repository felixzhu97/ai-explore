package com.ai.vision.presentation.dto;

import com.fasterxml.jackson.annotation.JsonInclude;

/**
 * Unified request DTO for image-based endpoints.
 * Supports both URL-based and base64-encoded image inputs.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public record ImageRequest(
    String imageUrl,
    String image,
    Float confidence,
    String language,
    String task,
    String engine
) {
    public ImageRequest {
        if (confidence == null) confidence = 0.25f;
        if (language == null) language = "eng";
        if (task == null) task = "caption_image";
        if (engine == null) engine = "easyocr";
    }

    public boolean hasImageUrl() {
        return imageUrl != null && !imageUrl.isBlank();
    }

    public boolean hasImage() {
        return image != null && !image.isBlank();
    }

    public boolean hasImageData() {
        return hasImageUrl() || hasImage();
    }
}
