package com.ai.agents.infrastructure.llm;

import com.ai.agents.domain.AgentType;
import com.ai.agents.domain.RoutingDecision;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.util.*;

/**
 * LLM-based supervisor router.
 * Uses keyword matching and LLM reasoning for routing decisions.
 */
@Component
public class SupervisorLLMRouter {

    private static final Logger log = LoggerFactory.getLogger(SupervisorLLMRouter.class);

    private static final Map<AgentType, Set<String>> AGENT_KEYWORDS;
    private static final Map<String, AgentType> IMPLICIT_ROUTING;

    static {
        Map<AgentType, Set<String>> keywords = new EnumMap<>(AgentType.class);
        keywords.put(AgentType.RAG, Set.of("文档", "知识库", "检索", "rag", "search", "document", "knowledge", "database", "查找", "查询"));
        keywords.put(AgentType.TTS, Set.of("语音", "朗读", "tts", "speak", "audio", "声音", "speech", "语音合成"));
        keywords.put(AgentType.VISION, Set.of("图片", "图像", "vision", "image", "analyze", "识别", "看图"));
        keywords.put(AgentType.MEDIA, Set.of("生成图片", "image generation", "generate image", "create image", "绘图"));
        keywords.put(AgentType.TEXT, Set.of("翻译", "translate", "summarize", "总结", "摘要", "rewrite", "改写"));
        keywords.put(AgentType.SUPERVISOR, Set.of());
        AGENT_KEYWORDS = Collections.unmodifiableMap(keywords);

        Map<String, AgentType> routing = new HashMap<>();
        routing.put("k8s", AgentType.SUPERVISOR);
        routing.put("kubernetes", AgentType.SUPERVISOR);
        routing.put("pod", AgentType.SUPERVISOR);
        routing.put("deployment", AgentType.SUPERVISOR);
        routing.put("aiops", AgentType.SUPERVISOR);
        routing.put("anomaly", AgentType.SUPERVISOR);
        routing.put("incident", AgentType.SUPERVISOR);
        routing.put("llmops", AgentType.SUPERVISOR);
        routing.put("model", AgentType.SUPERVISOR);
        routing.put("training", AgentType.SUPERVISOR);
        routing.put("pipeline", AgentType.SUPERVISOR);
        routing.put("workflow", AgentType.SUPERVISOR);
        routing.put("feature", AgentType.SUPERVISOR);
        routing.put("monitoring", AgentType.SUPERVISOR);
        routing.put("metric", AgentType.SUPERVISOR);
        routing.put("alert", AgentType.SUPERVISOR);
        routing.put("vector", AgentType.SUPERVISOR);
        routing.put("embedding", AgentType.SUPERVISOR);
        routing.put("chroma", AgentType.SUPERVISOR);
        routing.put("video", AgentType.SUPERVISOR);
        IMPLICIT_ROUTING = Collections.unmodifiableMap(routing);
    }

    public Mono<RoutingDecision> route(String message) {
        return Mono.fromCallable(() -> {
            if (message == null || message.isBlank()) {
                log.debug("Empty message, defaulting to CHAT");
                return RoutingDecision.fallback();
            }

            String lowerMessage = message.toLowerCase();
            Map<AgentType, Integer> matches = new EnumMap<>(AgentType.class);

            for (Map.Entry<String, AgentType> entry : IMPLICIT_ROUTING.entrySet()) {
                if (lowerMessage.contains(entry.getKey())) {
                    AgentType type = entry.getValue();
                    matches.merge(type, 1, Integer::sum);
                }
            }

            for (Map.Entry<AgentType, Set<String>> entry : AGENT_KEYWORDS.entrySet()) {
                AgentType type = entry.getKey();
                for (String keyword : entry.getValue()) {
                    if (lowerMessage.contains(keyword)) {
                        matches.merge(type, 1, Integer::sum);
                    }
                }
            }

            if (matches.isEmpty()) {
                log.debug("No keywords matched, defaulting to CHAT");
                return RoutingDecision.fallback();
            }

            AgentType bestMatch = matches.entrySet().stream()
                    .max(Map.Entry.comparingByValue())
                    .map(Map.Entry::getKey)
                    .orElse(AgentType.CHAT);

            int matchCount = matches.get(bestMatch);
            double confidence = Math.min(0.5 + (matchCount * 0.1), 0.95);

            log.info("Routing decision: {} (confidence: {}, matches: {})", bestMatch, confidence, matchCount);
            return RoutingDecision.to(bestMatch, confidence, "Keyword matching: " + matchCount + " matches");
        });
    }

    public Mono<RoutingDecision> routeTo(AgentType targetType, String message) {
        return Mono.fromCallable(() -> {
            if (targetType == null) {
                return RoutingDecision.fallback();
            }

            if (message != null && !message.isBlank()) {
                String lowerMessage = message.toLowerCase();
                Set<String> keywords = AGENT_KEYWORDS.getOrDefault(targetType, Set.of());

                for (String keyword : keywords) {
                    if (lowerMessage.contains(keyword)) {
                        return RoutingDecision.to(targetType, 1.0, "Explicit routing with keyword match");
                    }
                }
            }

            return RoutingDecision.of(targetType);
        });
    }

    public RoutingExplanation explain(String message) {
        if (message == null || message.isBlank()) {
            return new RoutingExplanation(message, List.of(), AgentType.CHAT, 0.5);
        }

        String lowerMessage = message.toLowerCase();
        List<String> matchedKeywords = new ArrayList<>();
        Map<AgentType, Integer> scores = new EnumMap<>(AgentType.class);

        for (Map.Entry<String, AgentType> entry : IMPLICIT_ROUTING.entrySet()) {
            if (lowerMessage.contains(entry.getKey())) {
                matchedKeywords.add(entry.getKey());
                scores.merge(entry.getValue(), 2, Integer::sum);
            }
        }

        for (Map.Entry<AgentType, Set<String>> entry : AGENT_KEYWORDS.entrySet()) {
            AgentType type = entry.getKey();
            for (String keyword : entry.getValue()) {
                if (lowerMessage.contains(keyword)) {
                    matchedKeywords.add(keyword);
                    scores.merge(type, 1, Integer::sum);
                }
            }
        }

        AgentType routedTo = scores.entrySet().stream()
                .max(Map.Entry.comparingByValue())
                .map(Map.Entry::getKey)
                .orElse(AgentType.CHAT);

        int totalScore = scores.values().stream().mapToInt(Integer::intValue).sum();
        double confidence = totalScore > 0 ? Math.min(0.5 + (totalScore * 0.1), 0.95) : 0.5;

        return new RoutingExplanation(message, matchedKeywords, routedTo, confidence);
    }

    public record RoutingExplanation(
            String message,
            List<String> matchedKeywords,
            AgentType routedTo,
            double confidence
    ) {}
}
