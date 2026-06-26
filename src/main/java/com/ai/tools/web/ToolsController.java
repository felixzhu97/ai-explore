package com.ai.tools.web;

import com.ai.tools.application.usecase.ToolsFacade;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Tools REST Controller for weather and document search.
 */
@RestController
@RequestMapping("/api")
public class ToolsController {

    private static final Logger log = LoggerFactory.getLogger(ToolsController.class);

    private final ToolsFacade toolsFacade;

    public ToolsController(ToolsFacade toolsFacade) {
        this.toolsFacade = toolsFacade;
    }

    /**
     * Get weather for a city.
     */
    @GetMapping("/tools/weather")
    public String getWeather(@RequestParam String city) {
        return toolsFacade.getWeather(city);
    }

    /**
     * Get weather forecast.
     */
    @GetMapping("/tools/weather/forecast")
    public String getForecast(
            @RequestParam String city,
            @RequestParam(required = false) Integer days) {
        return toolsFacade.getForecast(city, days);
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
        return toolsFacade.searchDocuments(query, docIdList);
    }

    /**
     * List all documents in knowledge base.
     */
    @GetMapping("/tools/documents/list")
    public String listDocuments() {
        return toolsFacade.listDocuments();
    }

    /**
     * Chat with function calling.
     */
    @PostMapping("/tools/chat")
    public ToolChatResponse chatWithTools(@RequestBody ToolChatRequest request) {
        try {
            String response = toolsFacade.chatWithTools(request.question());
            return new ToolChatResponse(response, null);
        } catch (Exception e) {
            log.error("Error in chat with tools", e);
            return new ToolChatResponse("抱歉，处理您的请求时发生错误，请稍后重试。", null);
        }
    }

    public record ToolChatRequest(String question, List<String> docIds) {}

    public record ToolChatResponse(String answer, List<String> toolCalls) {}
}
