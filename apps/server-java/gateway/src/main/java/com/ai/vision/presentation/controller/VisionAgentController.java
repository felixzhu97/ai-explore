package com.ai.vision.presentation.controller;

import com.ai.vision.application.service.VisionApplicationService;
import com.ai.vision.domain.ModelType;
import com.ai.vision.presentation.dto.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.buffer.DataBufferUtils;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.codec.multipart.FilePart;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * Vision Agent REST Controller.
 * 
 * Thin presentation layer that:
 * 1. Receives HTTP requests (multipart/form-data)
 * 2. Converts to domain types (ImageData)
 * 3. Delegates to Application Service
 * 4. Returns DTO responses
 * 
 * API endpoints:
 * - POST /api/vision/detect - Object detection (YOLO)
 * - POST /api/vision/caption - Image captioning (BLIP)
 * - POST /api/vision/ocr - Text recognition (OCR)
 * - POST /api/vision/generate - Image generation (Stable Diffusion)
 * - POST /api/vision/analyze - Combined vision analysis
 * - GET /api/vision/health - Provider health check
 * - GET /api/vision/capabilities - Available capabilities
 */
@RestController
@RequestMapping("/api/vision")
public class VisionAgentController {

    private static final Logger log = LoggerFactory.getLogger(VisionAgentController.class);

    private final VisionApplicationService visionService;

    public VisionAgentController(VisionApplicationService visionService) {
        this.visionService = visionService;
    }

    /**
     * Object detection endpoint using YOLO.
     * 
     * Python API: POST /api/vision/detect
     * 
     * @param image The image file to analyze
     * @param confidence Confidence threshold (0.0-1.0)
     * @return Detection results with bounding boxes
     */
    @PostMapping(value = "/detect", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public Mono<ResponseEntity<DetectionResponse>> detect(
            @RequestPart("image") FilePart image,
            @RequestParam(value = "confidence", defaultValue = "0.5") float confidence) {
        
        log.info("Received detect request, confidence: {}", confidence);

        return extractBytesFromFilePart(image)
            .flatMap(imageBytes -> visionService.detectObjects(imageBytes, confidence))
            .map(ResponseEntity::ok)
            .onErrorResume(e -> {
                log.error("Detection failed: {}", e.getMessage());
                return Mono.just(ResponseEntity.internalServerError().build());
            });
    }

    /**
     * Image captioning endpoint using BLIP.
     * 
     * Python API: POST /api/vision/caption
     * 
     * @param image The image file to caption
     * @return Caption/description of the image
     */
    @PostMapping(value = "/caption", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public Mono<ResponseEntity<CaptionResponse>> caption(
            @RequestPart("image") FilePart image) {
        
        log.info("Received caption request");

        return extractBytesFromFilePart(image)
            .flatMap(imageBytes -> visionService.captionImage(imageBytes))
            .map(ResponseEntity::ok)
            .onErrorResume(e -> {
                log.error("Captioning failed: {}", e.getMessage());
                return Mono.just(ResponseEntity.internalServerError().build());
            });
    }

    /**
     * OCR text recognition endpoint.
     * 
     * Python API: POST /api/vision/ocr
     * 
     * @param image The image file containing text
     * @param language Language code (e.g., "eng", "chi_sim")
     * @return Recognized text with confidence
     */
    @PostMapping(value = "/ocr", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public Mono<ResponseEntity<OcrResponse>> ocr(
            @RequestPart("image") FilePart image,
            @RequestParam(value = "language", defaultValue = "eng") String language) {
        
        log.info("Received OCR request, language: {}", language);

        return extractBytesFromFilePart(image)
            .flatMap(imageBytes -> visionService.recognizeText(imageBytes, language))
            .map(ResponseEntity::ok)
            .onErrorResume(e -> {
                log.error("OCR failed: {}", e.getMessage());
                return Mono.just(ResponseEntity.internalServerError().build());
            });
    }

    /**
     * Image generation endpoint using Stable Diffusion.
     * 
     * Python API: POST /api/vision/generate
     * 
     * @param request Generation parameters
     * @return Generated image URL
     */
    @PostMapping(value = "/generate", consumes = MediaType.APPLICATION_JSON_VALUE)
    public Mono<ResponseEntity<GenerateResponse>> generate(
            @RequestBody GenerateRequest request) {
        
        log.info("Received generate request, prompt: {}", request.prompt());

        return visionService.generateImage(request)
            .map(ResponseEntity::ok)
            .onErrorResume(e -> {
                log.error("Generation failed: {}", e.getMessage());
                return Mono.just(ResponseEntity.internalServerError().build());
            });
    }

    /**
     * Combined vision analysis endpoint.
     * Performs multiple vision tasks on a single image.
     */
    @PostMapping(value = "/analyze", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public Mono<ResponseEntity<Map<String, Object>>> analyze(
            @RequestPart("image") FilePart image,
            @RequestParam(value = "tasks", defaultValue = "detect,caption") String tasks) {
        
        log.info("Received analyze request, tasks: {}", tasks);

        return extractBytesFromFilePart(image)
            .flatMap(imageBytes -> {
                List<String> taskList = Arrays.stream(tasks.split(","))
                    .map(String::trim)
                    .map(String::toLowerCase)
                    .toList();
                
                return visionService.analyzeImage(imageBytes, taskList);
            })
            .map(ResponseEntity::ok)
            .onErrorResume(e -> {
                log.error("Analysis failed: {}", e.getMessage());
                return Mono.just(ResponseEntity.internalServerError()
                    .body(Map.of("error", e.getMessage())));
            });
    }

    /**
     * Health check endpoint for vision providers.
     */
    @GetMapping("/health")
    public ResponseEntity<Map<String, Object>> health() {
        Map<ModelType, Boolean> status = visionService.getProviderStatus();
        
        boolean allHealthy = status.values().stream().allMatch(Boolean::booleanValue);
        boolean anyHealthy = status.values().stream().anyMatch(Boolean::booleanValue);
        
        String overallStatus = allHealthy ? "healthy" : (anyHealthy ? "degraded" : "unhealthy");
        
        return ResponseEntity.ok(Map.of(
            "status", overallStatus,
            "providers", status
        ));
    }

    /**
     * List available vision capabilities.
     */
    @GetMapping("/capabilities")
    public ResponseEntity<Map<String, Object>> capabilities() {
        return ResponseEntity.ok(Map.of(
            "detection", Map.of(
                "model", "YOLO",
                "available", visionService.isProviderAvailable(ModelType.YOLO),
                "endpoint", "/api/vision/detect"
            ),
            "captioning", Map.of(
                "model", "BLIP",
                "available", visionService.isProviderAvailable(ModelType.BLIP),
                "endpoint", "/api/vision/caption"
            ),
            "ocr", Map.of(
                "model", "Tesseract",
                "available", visionService.isProviderAvailable(ModelType.OCR),
                "endpoint", "/api/vision/ocr",
                "languages", List.of("eng", "chi_sim", "jpn", "kor")
            ),
            "generation", Map.of(
                "model", "Stable Diffusion",
                "available", visionService.isProviderAvailable(ModelType.STABLE_DIFFUSION),
                "endpoint", "/api/vision/generate"
            )
        ));
    }

    /**
     * Extract bytes from FilePart using collectList approach.
     */
    private Mono<byte[]> extractBytesFromFilePart(FilePart filePart) {
        return Flux.from(filePart.content())
            .collectList()
            .map(dataBuffers -> {
                int totalSize = dataBuffers.stream()
                    .mapToInt(db -> db.readableByteCount())
                    .sum();
                byte[] result = new byte[totalSize];
                int offset = 0;
                for (var db : dataBuffers) {
                    int length = db.readableByteCount();
                    db.read(result, offset, length);
                    offset += length;
                }
                return result;
            });
    }
}
