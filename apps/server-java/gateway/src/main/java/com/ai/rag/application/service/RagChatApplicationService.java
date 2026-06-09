package com.ai.rag.application.service;

import com.ai.rag.application.dto.ChatRequest;
import com.ai.rag.application.dto.ChatResponseDto;
import com.ai.rag.domain.SourceDocument;
import com.ai.rag.domain.VectorStore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;

import java.util.List;

/**
 * Application service for RAG chat operations.
 * Thin orchestration layer - delegates to domain objects.
 */
@Service
public class RagChatApplicationService {

    private static final Logger log = LoggerFactory.getLogger(RagChatApplicationService.class);

    private final VectorStore vectorStore;

    public RagChatApplicationService(VectorStore vectorStore) {
        this.vectorStore = vectorStore;
    }

    /**
     * Non-streaming chat with RAG context.
     */
    public ChatResponseDto chat(ChatRequest request) {
        List<SourceDocument> sources = searchSources(request.query(), request.effectiveTopK());

        if (sources.isEmpty()) {
            return ChatResponseDto.of(
                    "I don't have relevant information in my knowledge base to answer this question.",
                    request.sessionId(),
                    List.of()
            );
        }

        // Build context from sources
        String context = sources.stream()
                .map(SourceDocument::text)
                .reduce((a, b) -> a + "\n\n---\n\n" + b)
                .orElse("");

        // Note: Full implementation requires LLM integration
        // For now, return context as the answer
        String answer = "Based on the available information:\n\n" + context;
        
        return ChatResponseDto.of(answer, request.sessionId(), sources);
    }

    /**
     * Streaming chat with RAG context.
     */
    public Flux<String> streamChat(ChatRequest request) {
        return Flux.create(emitter -> {
            try {
                List<SourceDocument> sources = searchSources(request.query(), request.effectiveTopK());

                if (sources.isEmpty()) {
                    emitter.next("I don't have relevant information in my knowledge base to answer this question.");
                    emitter.complete();
                    return;
                }

                String context = sources.stream()
                        .map(SourceDocument::text)
                        .reduce((a, b) -> a + "\n\n---\n\n" + b)
                        .orElse("");

                emitter.next("Based on the available information:\n\n" + context);
                emitter.complete();

            } catch (Exception e) {
                log.error("Error in stream chat", e);
                emitter.error(e);
            }
        });
    }

    /**
     * Search for source documents without generating a response.
     */
    public List<SourceDocument> searchSources(String query, int topK) {
        return vectorStore.searchWithScores(query, topK);
    }

    /**
     * Gets vector store statistics.
     */
    public java.util.Map<String, Object> getStats() {
        return vectorStore.getStats();
    }
}
