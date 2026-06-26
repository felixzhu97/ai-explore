package com.ai.ai.application.usecase;

import com.ai.ai.domain.model.ChatMessage;
import com.ai.ai.domain.model.ChatSession;
import com.ai.ai.infrastructure.tools.WeatherTools;
import com.ai.rag.infrastructure.tools.RagSearchTool;
import com.ai.ai.web.dto.TextAnalysisResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

/**
 * Facade aggregating all AI capabilities.
 * Provides a unified interface for all AI operations.
 */
@Service
public class AiFacade {

    private static final Logger log = LoggerFactory.getLogger(AiFacade.class);

    private final SpringAiChatUseCase chatUseCase;
    private final SpringAiStructuredOutputUseCase structuredOutputUseCase;
    private final SpringAiImageGenerationUseCase imageGenerationUseCase;
    private final TextToSpeechUseCase textToSpeechUseCase;
    private final ChatClient chatClient;
    private final WeatherTools weatherTools;
    private final RagSearchTool ragSearchTool;

    public AiFacade(
            SpringAiChatUseCase chatUseCase,
            SpringAiStructuredOutputUseCase structuredOutputUseCase,
            SpringAiImageGenerationUseCase imageGenerationUseCase,
            TextToSpeechUseCase textToSpeechUseCase,
            ChatClient.Builder chatClientBuilder,
            WeatherTools weatherTools,
            RagSearchTool ragSearchTool) {
        this.chatUseCase = chatUseCase;
        this.structuredOutputUseCase = structuredOutputUseCase;
        this.imageGenerationUseCase = imageGenerationUseCase;
        this.textToSpeechUseCase = textToSpeechUseCase;
        this.chatClient = chatClientBuilder.build();
        this.weatherTools = weatherTools;
        this.ragSearchTool = ragSearchTool;
    }

    // ========== Chat Operations ==========

    /**
     * Simple chat without session context.
     */
    public String chat(String message) {
        log.info("AiFacade.chat: {}", truncate(message));
        return chatUseCase.chat(message);
    }

    /**
     * Chat with session context.
     */
    public String chatWithSession(String sessionId, String message) {
        log.info("AiFacade.chatWithSession: session={}, message={}", sessionId, truncate(message));
        return chatUseCase.chatWithSession(sessionId, message);
    }

    /**
     * Chat with default session.
     */
    public String chatWithSession(String message) {
        log.info("AiFacade.chatWithSession (default): {}", truncate(message));
        return chatUseCase.chatWithSession(message);
    }

    // ========== Session Operations ==========

    /**
     * Create a new chat session.
     */
    public ChatSession createSession(String title) {
        log.info("AiFacade.createSession: {}", title);
        return chatUseCase.createSession(title);
    }

    /**
     * Get session by ID.
     */
    public Optional<ChatSession> getSession(String sessionId) {
        log.info("AiFacade.getSession: {}", sessionId);
        return chatUseCase.getSession(sessionId);
    }

    /**
     * Get session message history.
     */
    public List<ChatMessage> getSessionHistory(String sessionId) {
        log.info("AiFacade.getSessionHistory: {}", sessionId);
        return chatUseCase.getSessionHistory(sessionId);
    }

    /**
     * Delete a session.
     */
    public void deleteSession(String sessionId) {
        log.info("AiFacade.deleteSession: {}", sessionId);
        chatUseCase.deleteSession(sessionId);
    }

    /**
     * Get all sessions.
     */
    public List<ChatSession> getAllSessions() {
        log.info("AiFacade.getAllSessions");
        return chatUseCase.getAllSessions();
    }

    // ========== Text Analysis ==========

    /**
     * Analyze text and return structured result.
     */
    public TextAnalysisResult analyzeText(String text) {
        log.info("AiFacade.analyzeText: {}", truncate(text));
        return structuredOutputUseCase.analyzeText(text);
    }

    /**
     * Analyze text with specified language.
     */
    public TextAnalysisResult analyzeTextWithLanguage(String text, String language) {
        log.info("AiFacade.analyzeTextWithLanguage: {} lang={}", truncate(text), language);
        return structuredOutputUseCase.analyzeTextWithLanguage(text, language);
    }

    // ========== Image Generation ==========

    /**
     * Generate an image from text prompt.
     */
    public String generateImage(String prompt, String model, String quality, int width, int height, int n) {
        log.info("AiFacade.generateImage: {}", truncate(prompt));
        return imageGenerationUseCase.generateImage(prompt, model, quality, width, height, n);
    }

    /**
     * Get available image generation models.
     */
    public List<String> getAvailableImageModels() {
        return imageGenerationUseCase.getAvailableModels();
    }

    /**
     * Get available image sizes.
     */
    public List<String> getAvailableImageSizes() {
        return imageGenerationUseCase.getAvailableSizes();
    }

    /**
     * Get available image qualities.
     */
    public List<String> getAvailableImageQualities() {
        return imageGenerationUseCase.getAvailableQualities();
    }

    // ========== Text-to-Speech ==========

    /**
     * Synthesize text to speech.
     */
    public byte[] synthesize(String text) {
        log.info("AiFacade.synthesize: {}", truncate(text));
        return textToSpeechUseCase.synthesize(text);
    }

    /**
     * Get available TTS voices.
     */
    public List<String> getAvailableVoices() {
        return textToSpeechUseCase.getAvailableVoices();
    }

    /**
     * Get available TTS models.
     */
    public List<String> getAvailableTtsModels() {
        return textToSpeechUseCase.getAvailableModels();
    }

    // ========== Tool Calling ==========

    /**
     * Chat with function calling (tools).
     */
    public String chatWithTools(String question) {
        log.info("AiFacade.chatWithTools: {}", truncate(question));
        return chatClient.prompt()
                .user(question)
                .tools(weatherTools, ragSearchTool)
                .call()
                .content();
    }

    /**
     * Get weather for a city.
     */
    public String getWeather(String city) {
        log.info("AiFacade.getWeather: {}", city);
        return weatherTools.getWeather(city);
    }

    /**
     * Get weather forecast.
     */
    public String getForecast(String city, Integer days) {
        log.info("AiFacade.getForecast: {} days={}", city, days);
        return weatherTools.getForecast(city, days);
    }

    /**
     * Search documents in knowledge base.
     */
    public String searchDocuments(String query, List<String> docIds) {
        log.info("AiFacade.searchDocuments: {}", truncate(query));
        return ragSearchTool.searchDocuments(query, docIds);
    }

    /**
     * List all documents in knowledge base.
     */
    public String listDocuments() {
        log.info("AiFacade.listDocuments");
        return ragSearchTool.listDocuments();
    }

    private String truncate(String text) {
        if (text == null) return "null";
        if (text.length() <= 50) return text;
        return text.substring(0, 50) + "...";
    }
}
