package com.ai.media.presentation.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

/**
 * DTO for image generation responses.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public record ImageGenerationResponse(
        List<String> images,
        long seed,
        String model,
        String prompt,
        int width,
        int height,
        @JsonProperty("num_inference_steps") int numInferenceSteps,
        @JsonProperty("guidance_scale") float guidanceScale,
        @JsonProperty("processing_time_ms") double processingTimeMs,
        boolean success,
        String error
) {
    public ImageGenerationResponse(
            List<String> images,
            long seed,
            String model,
            String prompt,
            int width,
            int height,
            int numInferenceSteps,
            float guidanceScale,
            double processingTimeMs
    ) {
        this(images, seed, model, prompt, width, height, numInferenceSteps, 
                guidanceScale, processingTimeMs, true, null);
    }

    public static ImageGenerationResponse error(String errorMessage) {
        return new ImageGenerationResponse(
                List.of("data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAAAAEAAAABCAYAAAAfFcSJAAAADUlEQVR42mNk+M9QDwADhgGAWjR9awAAAABJRU5ErkJggg=="),
                System.currentTimeMillis() % Integer.MAX_VALUE,
                "error",
                "error",
                512, 512, 25, 7.5f,
                0,
                false,
                errorMessage
        );
    }
}
