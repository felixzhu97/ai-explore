package com.ai.infrastructure.config;

import com.ai.application.port.AiChatPort;
import com.ai.application.port.ChatSessionRepositoryPort;
import com.ai.application.port.DocumentRepositoryPort;
import com.ai.application.port.EmbeddingPort;
import com.ai.application.port.VectorSearchPort;
import com.ai.application.service.ChatApplicationService;
import com.ai.application.service.RagApplicationService;
import com.ai.application.usecase.DeleteDocumentUseCase;
import com.ai.application.usecase.RagChatUseCase;
import com.ai.application.usecase.UploadDocumentUseCase;
import com.ai.domain.service.AiChatService;
import com.ai.infrastructure.adapter.ai.SpringAiChatAdapter;
import com.ai.infrastructure.adapter.ai.SpringAiChatService;
import com.ai.infrastructure.adapter.embedding.DeepSeekEmbeddingAdapter;
import com.ai.infrastructure.adapter.embedding.MockEmbeddingAdapter;
import com.ai.infrastructure.adapter.persistence.InMemoryChatSessionRepository;
import com.ai.infrastructure.adapter.persistence.JpaDocumentRepository;
import com.ai.infrastructure.adapter.vector.PgVectorAdapter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

/**
 * Application configuration class - manages dependency injection.
 * Connects infrastructure layer with domain/application layers.
 */
@Configuration
public class ApplicationConfig {

    @Bean
    public AiChatService aiChatService(SpringAiChatService springAiChatService) {
        return springAiChatService;
    }

    @Bean
    public AiChatPort aiChatPort(SpringAiChatAdapter springAiChatAdapter) {
        return springAiChatAdapter;
    }

    @Bean
    public InMemoryChatSessionRepository chatSessionRepository() {
        return new InMemoryChatSessionRepository();
    }

    @Bean
    public ChatApplicationService chatApplicationService(
            ChatSessionRepositoryPort repositoryPort,
            AiChatPort aiChatPort) {
        return new ChatApplicationService(repositoryPort, aiChatPort);
    }

    // RAG Infrastructure Beans

    @Bean
    public JpaDocumentRepository jpaDocumentRepository(
            com.ai.infrastructure.adapter.persistence.SpringDataDocumentRepository documentRepository,
            com.ai.infrastructure.adapter.persistence.SpringDataChunkRepository chunkRepository,
            com.fasterxml.jackson.databind.ObjectMapper objectMapper) {
        return new JpaDocumentRepository(documentRepository, chunkRepository, objectMapper);
    }

    @Bean
    public DocumentRepositoryPort documentRepositoryPort(JpaDocumentRepository jpaDocumentRepository) {
        return jpaDocumentRepository;
    }

    @Bean
    public EmbeddingPort embeddingPort(DeepSeekEmbeddingAdapter deepSeekEmbeddingAdapter, 
                                       MockEmbeddingAdapter mockEmbeddingAdapter,
                                       org.springframework.core.env.Environment env) {
        boolean useMock = Boolean.parseBoolean(
            env.getProperty("rag.mock.embeddings", "false"));
        if (useMock) {
            return mockEmbeddingAdapter;
        }
        return deepSeekEmbeddingAdapter;
    }

    @Bean
    public VectorSearchPort vectorSearchPort(PgVectorAdapter pgVectorAdapter) {
        return pgVectorAdapter;
    }

    // RAG Use Cases

    @Bean
    public UploadDocumentUseCase uploadDocumentUseCase(
            DocumentRepositoryPort documentRepositoryPort,
            EmbeddingPort embeddingPort,
            VectorSearchPort vectorSearchPort) {
        return new UploadDocumentUseCase(documentRepositoryPort, embeddingPort, vectorSearchPort);
    }

    @Bean
    public DeleteDocumentUseCase deleteDocumentUseCase(DocumentRepositoryPort documentRepositoryPort) {
        return new DeleteDocumentUseCase(documentRepositoryPort);
    }

    @Bean
    public RagChatUseCase ragChatUseCase(EmbeddingPort embeddingPort, VectorSearchPort vectorSearchPort) {
        return new RagChatUseCase(embeddingPort, vectorSearchPort);
    }

    @Bean
    public RagApplicationService ragApplicationService(
            UploadDocumentUseCase uploadDocumentUseCase,
            DeleteDocumentUseCase deleteDocumentUseCase,
            RagChatUseCase ragChatUseCase,
            DocumentRepositoryPort documentRepositoryPort) {
        return new RagApplicationService(uploadDocumentUseCase, deleteDocumentUseCase, ragChatUseCase, documentRepositoryPort);
    }
}
