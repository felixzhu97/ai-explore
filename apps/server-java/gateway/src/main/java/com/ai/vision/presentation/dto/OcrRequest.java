package com.ai.vision.presentation.dto;

import com.fasterxml.jackson.annotation.JsonInclude;

/**
 * Request DTO for OCR text recognition.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public record OcrRequest(
    boolean includeBboxes,
    String language
) {
    public OcrRequest {
        if (language == null) language = "eng";
    }

    public static OcrRequest defaultConfig() {
        return new OcrRequest(false, "eng");
    }
}
