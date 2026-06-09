package com.ai.vision.presentation.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record OcrResponse(
    String task,
    String model,
    List<TextBlock> results,
    @JsonProperty("full_text") String fullText,
    @JsonProperty("processing_time_ms") double processingTimeMs
) {
    public record TextBlock(
        String text,
        float confidence,
        List<List<Float>> bbox
    ) {}

    public static OcrResponse of(String model, List<TextBlock> results, double processingTimeMs) {
        String fullText = results.stream()
            .map(TextBlock::text)
            .reduce((a, b) -> a + "\n" + b)
            .orElse("");
        return new OcrResponse("extract_text", model, results, fullText, processingTimeMs);
    }
}
