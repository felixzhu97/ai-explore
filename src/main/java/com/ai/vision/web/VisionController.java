package com.ai.vision.web;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestClient;
import org.springframework.web.multipart.MultipartFile;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.util.Base64;
import java.util.List;
import java.util.Map;

/**
 * Vision/image understanding controller.
 * Uses OpenAI GPT-4o for image analysis (caption, object detection, OCR).
 */
@RestController
@RequestMapping("/api/vision")
public class VisionController {

    private static final Logger log = LoggerFactory.getLogger(VisionController.class);

    private final RestClient restClient;
    private final String visionModel;

    public VisionController(@Value("${spring.ai.openai.api-key:}") String apiKey,
                            @Value("${spring.ai.openai.base-url:https://api.openai.com}") String baseUrl,
                            @Value("${vision.model:gpt-4o-mini}") String visionModel) {
        this.visionModel = visionModel;
        this.restClient = RestClient.builder()
                .baseUrl(baseUrl)
                .defaultHeader("Authorization", "Bearer " + apiKey)
                .build();
    }

    @PostMapping(value = "/caption", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public Map<String, Object> captionImage(@RequestParam("file") MultipartFile file) throws IOException {
        long start = System.currentTimeMillis();
        String base64 = encodeImage(file);
        String caption = callVision("Describe this image in detail.", base64, file.getContentType());
        return Map.of(
                "caption", caption,
                "processing_time_ms", System.currentTimeMillis() - start
        );
    }

    @PostMapping(value = "/detect", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public Map<String, Object> detectObjects(@RequestParam("file") MultipartFile file) throws IOException {
        String base64 = encodeImage(file);
        String result = callVision(
                "List all objects you see in this image. Return a JSON array of objects with fields: class_name, confidence (0-1). Only return the JSON array, nothing else.",
                base64, file.getContentType());
        try {
            var detections = parseJsonList(result);
            return Map.of("detections", detections);
        } catch (Exception e) {
            log.warn("Failed to parse detections, returning raw: {}", result);
            return Map.of("detections", List.of(Map.of("class_name", result, "confidence", 1.0)));
        }
    }

    @PostMapping(value = "/ocr", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public Map<String, Object> ocrImage(@RequestParam("file") MultipartFile file) throws IOException {
        long start = System.currentTimeMillis();
        String base64 = encodeImage(file);
        String text = callVision("Extract all text visible in this image. Return only the extracted text, nothing else.",
                base64, file.getContentType());
        return Map.of(
                "full_text", text,
                "processing_time_ms", System.currentTimeMillis() - start
        );
    }

    private String encodeImage(MultipartFile file) throws IOException {
        return Base64.getEncoder().encodeToString(file.getBytes());
    }

    private String callVision(String prompt, String base64Image, String contentType) {
        String dataUri = "data:" + (contentType != null ? contentType : "image/jpeg") + ";base64," + base64Image;

        var requestBody = Map.of(
                "model", visionModel,
                "messages", List.of(
                        Map.of("role", "user", "content", List.of(
                                Map.of("type", "text", "text", prompt),
                                Map.of("type", "image_url", "image_url",
                                        Map.of("url", dataUri))
                        ))
                ),
                "max_tokens", 500
        );

        log.info("Calling vision model {} for: {}", visionModel, prompt.substring(0, 50));
        var response = restClient.post()
                .uri("/chat/completions")
                .body(requestBody)
                .retrieve()
                .body(Map.class);

        if (response != null && response.containsKey("choices")) {
            var choices = (List<Map<String, Object>>) response.get("choices");
            if (!choices.isEmpty()) {
                var message = (Map<String, Object>) choices.get(0).get("message");
                return (String) message.get("content");
            }
        }
        return "No response from vision model";
    }

    @SuppressWarnings("unchecked")
    private List<Map<String, Object>> parseJsonList(String text) throws JsonProcessingException {
        text = text.trim();
        if (text.startsWith("```")) {
            text = text.substring(text.indexOf("\n") + 1);
            if (text.endsWith("```")) text = text.substring(0, text.lastIndexOf("```"));
        }
        return (List<Map<String, Object>>) new ObjectMapper().readValue(text, List.class);
    }
}
