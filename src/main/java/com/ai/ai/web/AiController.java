package com.ai.ai.web;

import com.ai.ai.application.usecase.AiFacade;
import com.ai.ai.web.dto.*;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * Unified AI REST Controller.
 * Consolidates chat, image generation, TTS, and tool calling endpoints.
 */
@RestController
@RequestMapping("/api")
public class AiController {

    private static final Logger log = LoggerFactory.getLogger(AiController.class);

    private final AiFacade facade;

    public AiController(AiFacade facade) {
        this.facade = facade;
    }

    // ========== Chat Endpoints ==========

    /**
     * Sends a chat message and receives AI response.
     */
    @PostMapping("/chat")
    public ResponseEntity<ChatResponse> chat(@Valid @RequestBody ChatRequest request) {
        if (request.message() == null || request.message().isBlank()) {
            return ResponseEntity.badRequest()
                .body(ChatResponse.of("Please provide a message."));
        }

        String response;
        if (request.sessionId() != null && !request.sessionId().isBlank()) {
            response = facade.chatWithSession(request.sessionId(), request.message());
        } else {
            response = facade.chatWithSession(request.message());
        }

        return ResponseEntity.ok(ChatResponse.of(response));
    }

    // ========== Session Endpoints ==========

    /**
     * Creates a new session.
     */
    @PostMapping("/sessions")
    public ResponseEntity<SessionInfo> createSession(@Valid @RequestBody(required = false) CreateSessionRequest body) {
        String title = body != null && body.title() != null ? body.title() : "New Chat";
        var session = facade.createSession(title);
        return ResponseEntity.ok(SessionInfo.from(session));
    }

    /**
     * Retrieves all sessions.
     */
    @GetMapping("/sessions")
    public ResponseEntity<List<SessionInfo>> getAllSessions() {
        List<SessionInfo> sessions = facade.getAllSessions()
            .stream()
            .map(SessionInfo::from)
            .toList();
        return ResponseEntity.ok(sessions);
    }

    /**
     * Deletes a session.
     */
    @DeleteMapping("/sessions/{sessionId}")
    public ResponseEntity<Void> deleteSession(@PathVariable String sessionId) {
        facade.deleteSession(sessionId);
        return ResponseEntity.noContent().build();
    }

    // ========== Analysis Endpoints ==========

    /**
     * Analyzes text and returns structured result.
     */
    @PostMapping("/chat/analyze")
    public ResponseEntity<TextAnalysisResult> analyzeText(@Valid @RequestBody TextAnalysisRequest request) {
        if (request.text() == null || request.text().isBlank()) {
            return ResponseEntity.badRequest().build();
        }

        TextAnalysisResult result;
        if (request.language() != null && !request.language().isBlank()) {
            result = facade.analyzeTextWithLanguage(request.text(), request.language());
        } else {
            result = facade.analyzeText(request.text());
        }

        return ResponseEntity.ok(result);
    }

    // ========== Image Generation Endpoints ==========

    /**
     * Generate an image from text prompt.
     */
    @PostMapping("/images/generate")
    public ResponseEntity<ImageGenerationResponse> generateImage(
            @Valid @RequestBody ImageGenerationRequest request) {
        try {
            String imageUrl = facade.generateImage(
                    request.prompt(),
                    request.model(),
                    request.quality(),
                    request.width() != null ? request.width() : 1024,
                    request.height() != null ? request.height() : 1024,
                    request.n() != null ? request.n() : 1
            );

            if (imageUrl == null) {
                return ResponseEntity.internalServerError()
                        .body(ImageGenerationResponse.error("Failed to generate image"));
            }

            return ResponseEntity.ok(ImageGenerationResponse.success(
                    imageUrl,
                    request.model() != null ? request.model() : "dall-e-3",
                    request.prompt()
            ));
        } catch (Exception e) {
            log.error("Error generating image", e);
            return ResponseEntity.internalServerError()
                    .body(ImageGenerationResponse.error("生成图片时发生错误，请稍后重试。"));
        }
    }

    /**
     * Get available image generation models.
     */
    @GetMapping("/images/models")
    public ResponseEntity<Map<String, List<String>>> getImageModels() {
        return ResponseEntity.ok(Map.of("models", facade.getAvailableImageModels()));
    }

    /**
     * Get available image sizes.
     */
    @GetMapping("/images/sizes")
    public ResponseEntity<Map<String, List<String>>> getImageSizes() {
        return ResponseEntity.ok(Map.of("sizes", facade.getAvailableImageSizes()));
    }

    /**
     * Get available image qualities.
     */
    @GetMapping("/images/qualities")
    public ResponseEntity<Map<String, List<String>>> getImageQualities() {
        return ResponseEntity.ok(Map.of("qualities", facade.getAvailableImageQualities()));
    }

    // ========== Audio/TTS Endpoints ==========

    /**
     * Convert text to speech.
     */
    @PostMapping(value = "/audio/speak", produces = MediaType.APPLICATION_OCTET_STREAM_VALUE)
    public ResponseEntity<byte[]> speak(@RequestBody TtsRequest request) {
        try {
            byte[] audio = facade.synthesize(request.text());

            if (audio == null || audio.length == 0) {
                return ResponseEntity.internalServerError().build();
            }

            return ResponseEntity.ok()
                    .contentType(MediaType.parseMediaType("audio/mpeg"))
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"speech.mp3\"")
                    .body(audio);
        } catch (Exception e) {
            log.error("Error synthesizing speech", e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Get available TTS voices.
     */
    @GetMapping("/audio/voices")
    public ResponseEntity<Map<String, Object>> getVoices() {
        return ResponseEntity.ok(Map.of("voices", facade.getAvailableVoices()));
    }

    /**
     * Get available TTS models.
     */
    @GetMapping("/audio/models")
    public ResponseEntity<Map<String, Object>> getTtsModels() {
        return ResponseEntity.ok(Map.of("models", facade.getAvailableTtsModels()));
    }

    // ========== Tool Calling Endpoints ==========

    /**
     * Get weather for a city.
     */
    @GetMapping("/tools/weather")
    public String getWeather(@RequestParam String city) {
        return facade.getWeather(city);
    }

    /**
     * Get weather forecast.
     */
    @GetMapping("/tools/weather/forecast")
    public String getForecast(
            @RequestParam String city,
            @RequestParam(required = false) Integer days) {
        return facade.getForecast(city, days);
    }

    /**
     * Search documents in knowledge base.
     */
    @GetMapping("/tools/documents/search")
    public String searchDocuments(
            @RequestParam String query,
            @RequestParam(required = false) String docIds) {
        List<String> docIdList = null;
        if (docIds != null && !docIds.isBlank()) {
            docIdList = List.of(docIds.split(","));
        }
        return facade.searchDocuments(query, docIdList);
    }

    /**
     * List all documents in knowledge base.
     */
    @GetMapping("/tools/documents/list")
    public String listDocuments() {
        return facade.listDocuments();
    }

    /**
     * Chat with function calling.
     */
    @PostMapping("/tools/chat")
    public ToolChatResponse chatWithTools(@RequestBody ToolChatRequest request) {
        try {
            String response = facade.chatWithTools(request.question());
            return new ToolChatResponse(response, null);
        } catch (Exception e) {
            log.error("Error in chat with tools", e);
            return new ToolChatResponse("抱歉，处理您的请求时发生错误，请稍后重试。", null);
        }
    }

    // ========== Health Endpoint ==========

    /**
     * Health check endpoint.
     */
    @GetMapping("/health")
    public ResponseEntity<HealthResponse> health() {
        return ResponseEntity.ok(HealthResponse.up());
    }

    // ========== Request/Response Records ==========

    public record TtsRequest(String text) {}

    public record ToolChatRequest(String question, List<String> docIds) {}

    public record ToolChatResponse(String answer, List<String> toolCalls) {}
}
