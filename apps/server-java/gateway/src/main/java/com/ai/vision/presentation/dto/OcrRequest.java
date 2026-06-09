package com.ai.vision.presentation.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Request DTO for OCR text recognition.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public record OcrRequest(
    @JsonProperty("include_bboxes") boolean includeBboxes,
    String language
) {
    public OcrRequest {
        if (language == null) language = "eng";
    }

    public static OcrRequest defaultConfig() {
        return new OcrRequest(false, "eng");
    }
}
