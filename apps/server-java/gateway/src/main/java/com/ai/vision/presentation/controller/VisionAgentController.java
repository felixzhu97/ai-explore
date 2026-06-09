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

import java.net.URI;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Base64;

@RestController
@RequestMapping("/api/vision")
public class VisionAgentController {

    private static final Logger log = LoggerFactory.getLogger(VisionAgentController.class);

    private final VisionApplicationService visionService;

    public VisionAgentController(VisionApplicationService visionService) {
        this.visionService = visionService;
    }

    @GetMapping("/")
    public ResponseEntity<Map<String, Object>> root() {
        return ResponseEntity.ok(Map.of(
            "service", "AI Vision Service",
            "version", "0.2.0",
            "capabilities", Map.of(
                "vision", List.of("object_detection", "image_captioning", "ocr"),
                "image_generation", List.of("text_to_image", "variation", "upscale"),
                "video", List.of("text_to_video", "image_to_video")
            ),
            "endpoints", Map.of(
                "health", "/api/vision/health",
                "vision", Map.of(
                    "detect", "/api/vision/detect",
                    "caption", "/api/vision/caption",
                    "ocr", "/api/vision/ocr",
                    "analyze", "/api/vision/analyze"
                ),
                "image_generation", Map.of(
                    "generate", "/api/vision/generate",
                    "variation", "/api/vision/variation",
                    "upscale", "/api/vision/upscale",
                    "models", "/api/vision/models",
                    "cache_clear", "/api/vision/cache/clear",
                    "health", "/api/vision/image-gen/health"
                ),
                "video", Map.of(
                    "generate", "/api/vision/video/generate",
                    "status", "/api/vision/video/status/{taskId}"
                )
            )
        ));
    }

    /**
     * Root-level health check (matches Python GET /health).
     */
    @GetMapping("/health")
    public ResponseEntity<Map<String, Object>> health() {
        Map<ModelType, Boolean> status = visionService.getProviderStatus();
        boolean allHealthy = status.values().stream().allMatch(Boolean::booleanValue);
        boolean anyHealthy = status.values().stream().anyMatch(Boolean::booleanValue);
        String overallStatus = allHealthy ? "healthy" : (anyHealthy ? "degraded" : "unhealthy");
        return ResponseEntity.ok(Map.of("status", overallStatus));
    }

    /**
     * Object detection endpoint using YOLO.
     * Supports both multipart/form-data (legacy) and application/json.
     * Python API: POST /vision/detect
     */
    @PostMapping(value = "/detect", consumes = {MediaType.APPLICATION_JSON_VALUE, MediaType.MULTIPART_FORM_DATA_VALUE})
    public Mono<ResponseEntity<DetectionResponse>> detect(
            @RequestBody(required = false) ImageRequest jsonRequest,
            @RequestPart(value = "image", required = false) FilePart image,
            @RequestParam(value = "conf", defaultValue = "0.25") float confidence) {
        
        log.info("Received detect request, confidence: {}", confidence);
        
        Mono<byte[]> imageDataMono;
        if (jsonRequest != null && jsonRequest.hasImageData()) {
            float conf = jsonRequest.confidence() != null ? jsonRequest.confidence() : confidence;
            imageDataMono = extractImageData(jsonRequest);
        } else if (image != null) {
            imageDataMono = extractBytesFromFilePart(image);
        } else {
            return Mono.just(ResponseEntity.badRequest().build());
        }
        
        return imageDataMono
            .flatMap(imageBytes -> visionService.detectObjects(imageBytes, confidence))
            .map(ResponseEntity::ok)
            .onErrorResume(e -> {
                log.error("Detection failed: {}", e.getMessage());
                return Mono.just(ResponseEntity.internalServerError().build());
            });
    }

    /**
     * Image captioning endpoint using BLIP.
     * Supports both multipart/form-data (legacy) and application/json.
     * Python API: POST /vision/caption
     */
    @PostMapping(value = "/caption", consumes = {MediaType.APPLICATION_JSON_VALUE, MediaType.MULTIPART_FORM_DATA_VALUE})
    public Mono<ResponseEntity<CaptionResponse>> caption(
            @RequestBody(required = false) ImageRequest jsonRequest,
            @RequestPart(value = "image", required = false) FilePart image) {
        
        log.info("Received caption request");
        
        Mono<byte[]> imageDataMono;
        if (jsonRequest != null && jsonRequest.hasImageData()) {
            imageDataMono = extractImageData(jsonRequest);
        } else if (image != null) {
            imageDataMono = extractBytesFromFilePart(image);
        } else {
            return Mono.just(ResponseEntity.badRequest().build());
        }
        
        return imageDataMono
            .flatMap(imageBytes -> visionService.captionImage(imageBytes))
            .map(ResponseEntity::ok)
            .onErrorResume(e -> {
                log.error("Captioning failed: {}", e.getMessage());
                return Mono.just(ResponseEntity.internalServerError().build());
            });
    }

    /**
     * OCR text recognition endpoint.
     * Supports both multipart/form-data (legacy) and application/json.
     * Python API: POST /vision/ocr
     */
    @PostMapping(value = "/ocr", consumes = {MediaType.APPLICATION_JSON_VALUE, MediaType.MULTIPART_FORM_DATA_VALUE})
    public Mono<ResponseEntity<OcrResponse>> ocr(
            @RequestBody(required = false) ImageRequest jsonRequest,
            @RequestPart(value = "image", required = false) FilePart image,
            @RequestParam(value = "engine", defaultValue = "easyocr") String engine) {
        
        log.info("Received OCR request, engine: {}", engine);
        
        Mono<byte[]> imageDataMono;
        if (jsonRequest != null && jsonRequest.hasImageData()) {
            imageDataMono = extractImageData(jsonRequest);
        } else if (image != null) {
            imageDataMono = extractBytesFromFilePart(image);
        } else {
            return Mono.just(ResponseEntity.badRequest().build());
        }
        
        return imageDataMono
            .flatMap(imageBytes -> visionService.recognizeText(imageBytes, engine))
            .map(ResponseEntity::ok)
            .onErrorResume(e -> {
                log.error("OCR failed: {}", e.getMessage());
                return Mono.just(ResponseEntity.internalServerError().build());
            });
    }

    /**
     * Image generation endpoint using Stable Diffusion.
     * Python API: POST /image-gen/generate
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
     * Image variation endpoint.
     * Python API: POST /image-gen/variation
     */
    @PostMapping(value = "/variation", consumes = MediaType.APPLICATION_JSON_VALUE)
    public Mono<ResponseEntity<Map<String, Object>>> variation(
            @RequestBody VariationRequest request) {
        
        log.info("Received variation request");
        return visionService.generateVariation(request)
            .map(ResponseEntity::ok)
            .onErrorResume(e -> {
                log.error("Variation generation failed: {}", e.getMessage());
                return Mono.just(ResponseEntity.internalServerError()
                    .body(Map.of("error", e.getMessage())));
            });
    }

    /**
     * Image upscale endpoint.
     * Python API: POST /image-gen/upscale
     */
    @PostMapping(value = "/upscale", consumes = MediaType.APPLICATION_JSON_VALUE)
    public Mono<ResponseEntity<Map<String, Object>>> upscale(
            @RequestBody UpscaleRequest request) {
        
        log.info("Received upscale request, scale: {}", request.scale());
        return visionService.upscaleImage(request)
            .map(ResponseEntity::ok)
            .onErrorResume(e -> {
                log.error("Upscale failed: {}", e.getMessage());
                return Mono.just(ResponseEntity.internalServerError()
                    .body(Map.of("error", e.getMessage())));
            });
    }

    /**
     * List available image generation models.
     * Python API: GET /image-gen/models
     */
    @GetMapping("/models")
    public ResponseEntity<Map<String, Object>> listModels() {
        return ResponseEntity.ok(Map.of(
            "models", List.of(Map.of(
                "model_id", "stable-diffusion-xl",
                "model_type", "text-to-image",
                "capabilities", List.of("text_to_image", "variation", "upscale"),
                "max_dimensions", List.of(2048, 2048),
                "recommended_steps", List.of(25, 50),
                "vram_required_gb", 8.0,
                "supports_attention_slicing", true,
                "supports_vae_slicing", true
            )),
            "default_model", "stable-diffusion-xl"
        ));
    }

    /**
     * Clear model cache.
     * Python API: POST /image-gen/cache/clear
     */
    @PostMapping("/cache/clear")
    public ResponseEntity<Map<String, Object>> clearCache() {
        return ResponseEntity.ok(Map.of("status", "ok", "message", "Cache cleared successfully"));
    }

    /**
     * Image generation health check.
     * Python API: GET /image-gen/health
     */
    @GetMapping("/image-gen/health")
    public ResponseEntity<Map<String, Object>> imageGenHealth() {
        return ResponseEntity.ok(Map.of(
            "status", "ok",
            "device", "cpu",
            "cuda_available", false,
            "cuda_device_count", 0
        ));
    }

    /**
     * Combined vision analysis endpoint.
     * Supports both multipart/form-data (legacy) and application/json.
     * Python API: POST /vision/analyze
     */
    @PostMapping(value = "/analyze", consumes = {MediaType.APPLICATION_JSON_VALUE, MediaType.MULTIPART_FORM_DATA_VALUE})
    public Mono<ResponseEntity<Map<String, Object>>> analyze(
            @RequestBody(required = false) ImageRequest jsonRequest,
            @RequestPart(value = "image", required = false) FilePart image,
            @RequestParam(value = "task", defaultValue = "caption_image") String task) {
        
        log.info("Received analyze request, task: {}", task);
        
        Mono<byte[]> imageDataMono;
        if (jsonRequest != null && jsonRequest.hasImageData()) {
            String analyzeTask = jsonRequest.task() != null ? jsonRequest.task() : task;
            imageDataMono = extractImageData(jsonRequest);
        } else if (image != null) {
            imageDataMono = extractBytesFromFilePart(image);
        } else {
            return Mono.just(ResponseEntity.badRequest().build());
        }
        
        return imageDataMono
            .flatMap(imageBytes -> visionService.analyzeImage(imageBytes, task))
            .map(ResponseEntity::ok)
            .onErrorResume(e -> {
                log.error("Analysis failed: {}", e.getMessage());
                return Mono.just(ResponseEntity.internalServerError()
                    .body(Map.of("error", e.getMessage())));
            });
    }

    // ========================================================================
    // Video Generation Endpoints (prefix: /api/vision/video)
    // ========================================================================

    /**
     * Generate video from text prompt.
     * Python API: POST /video/generate
     */
    @PostMapping(value = "/video/generate", consumes = MediaType.APPLICATION_JSON_VALUE)
    public Mono<ResponseEntity<VideoGenerateResponse>> generateVideo(
            @RequestBody VideoGenerateRequest request) {
        
        log.info("Received video generate request, prompt: {}", request.prompt());
        return visionService.generateVideo(request)
            .map(ResponseEntity::ok)
            .onErrorResume(e -> {
                log.error("Video generation failed: {}", e.getMessage());
                return Mono.just(ResponseEntity.internalServerError().build());
            });
    }

    /**
     * Generate video with advanced options.
     * Python API: POST /video/generate/advanced
     */
    @PostMapping(value = "/video/generate/advanced", consumes = MediaType.APPLICATION_JSON_VALUE)
    public Mono<ResponseEntity<VideoGenerateResponse>> generateVideoAdvanced(
            @RequestBody VideoGenerateRequest request) {
        
        log.info("Received advanced video generate request");
        return visionService.generateVideoAdvanced(request)
            .map(ResponseEntity::ok)
            .onErrorResume(e -> {
                log.error("Advanced video generation failed: {}", e.getMessage());
                return Mono.just(ResponseEntity.internalServerError().build());
            });
    }

    /**
     * Get video task status.
     * Python API: GET /video/status/{taskId}
     */
    @GetMapping("/video/status/{taskId}")
    public Mono<ResponseEntity<VideoStatusResponse>> getVideoStatus(
            @PathVariable String taskId) {
        
        log.info("Received video status request for task: {}", taskId);
        return visionService.getVideoStatus(taskId)
            .map(ResponseEntity::ok)
            .onErrorResume(e -> {
                log.error("Video status check failed: {}", e.getMessage());
                return Mono.just(ResponseEntity.status(404).build());
            });
    }

    // ========================================================================
    // Utility Methods
    // ========================================================================

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

    /**
     * Extract image bytes from ImageRequest (URL or base64).
     */
    private Mono<byte[]> extractImageData(ImageRequest request) {
        if (request.hasImageUrl()) {
            return fetchFromUrl(request.imageUrl());
        } else if (request.hasImage()) {
            return decodeBase64(request.image());
        } else {
            return Mono.error(new IllegalArgumentException("No image data provided"));
        }
    }

    /**
     * Fetch image from URL.
     */
    private Mono<byte[]> fetchFromUrl(String url) {
        return Mono.fromCallable(() -> {
            try {
                URI uri = new URI(url);
                byte[] data;
                if ("file".equalsIgnoreCase(uri.getScheme())) {
                    java.nio.file.Path path = java.nio.file.Paths.get(uri);
                    data = java.nio.file.Files.readAllBytes(path);
                } else {
                    java.net.URL imageUrl = uri.toURL();
                    java.net.HttpURLConnection connection = (java.net.HttpURLConnection) imageUrl.openConnection();
                    connection.setRequestProperty("User-Agent", "Mozilla/5.0");
                    connection.setConnectTimeout(10000);
                    connection.setReadTimeout(10000);
                    try (java.io.InputStream is = connection.getInputStream()) {
                        data = is.readAllBytes();
                    }
                }
                log.info("Fetched image from URL: {} ({} bytes)", url, data.length);
                return data;
            } catch (Exception e) {
                log.error("Failed to fetch image from URL: {}", url, e);
                throw new RuntimeException("Failed to fetch image from URL: " + e.getMessage(), e);
            }
        }).subscribeOn(reactor.core.scheduler.Schedulers.boundedElastic());
    }

    /**
     * Decode base64 encoded image string.
     */
    private Mono<byte[]> decodeBase64(String base64String) {
        return Mono.fromCallable(() -> {
            try {
                String cleanedBase64 = base64String;
                if (base64String.contains(",")) {
                    cleanedBase64 = base64String.substring(base64String.indexOf(",") + 1);
                }
                byte[] data = Base64.getDecoder().decode(cleanedBase64);
                log.info("Decoded base64 image ({} bytes)", data.length);
                return data;
            } catch (Exception e) {
                log.error("Failed to decode base64 image", e);
                throw new RuntimeException("Failed to decode base64 image: " + e.getMessage(), e);
            }
        }).subscribeOn(reactor.core.scheduler.Schedulers.boundedElastic());
    }
}
