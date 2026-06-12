package com.ai.application.usecase;

import com.ai.application.port.DocumentRepositoryPort;
import com.ai.application.port.EmbeddingPort;
import com.ai.application.port.VectorSearchPort;
import com.ai.domain.model.Document;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.lenient;

/**
 * UploadDocumentUseCaseChunkTextTest - Unit tests for chunkText boundary conditions.
 *
 * Naming convention: should_expected_result_when_condition
 * Uses AAA pattern (Arrange-Act-Assert)
 * Tests the private chunkText method through reflection.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("UploadDocumentUseCase chunkText Tests")
class UploadDocumentUseCaseChunkTextTest {

    @Mock
    private DocumentRepositoryPort documentRepository;

    @Mock
    private EmbeddingPort embeddingPort;

    @Mock
    private VectorSearchPort vectorSearchPort;

    private UploadDocumentUseCase useCase;

    @BeforeEach
    void setUp() {
        useCase = new UploadDocumentUseCase(documentRepository, embeddingPort, vectorSearchPort, 100, 20);
        // Use lenient stubbing because some tests only test chunkText via reflection
        lenient().when(documentRepository.save(any(Document.class))).thenAnswer(inv -> inv.getArgument(0));
        lenient().when(embeddingPort.embed(any(String.class))).thenReturn(new float[]{0.1f});
    }

    @Nested
    @DisplayName("chunkText boundary conditions")
    class ChunkTextBoundaryTests {

        @Test
        @DisplayName("should handle empty text")
        void shouldHandleEmptyText() {
            // Act
            List<String> chunks = invokeChunkText("");

            // Assert - empty string may produce one empty chunk after split
            // The actual behavior depends on how split handles empty strings
            assertThat(chunks).isNotNull();
        }

        @Test
        @DisplayName("should handle whitespace only text")
        void shouldHandleWhitespaceOnlyText() {
            // Act
            List<String> chunks = invokeChunkText("   \n\t  ");

            // Assert - whitespace text may produce empty chunks
            assertThat(chunks).isNotNull();
        }

        @Test
        @DisplayName("should return single chunk when text fits")
        void shouldReturnSingleChunkWhenTextFits() {
            // Act
            List<String> chunks = invokeChunkText("Short text.");

            // Assert
            assertThat(chunks).hasSize(1);
            assertThat(chunks.get(0)).isEqualTo("Short text.");
        }

        @Test
        @DisplayName("should split into multiple chunks when text exceeds size")
        void shouldSplitIntoMultipleChunksWhenTextExceedsSize() {
            // Arrange - Create text that exceeds chunk size
            // Using 100 char chunk size, need sentences that accumulate beyond that
            String text = "This is sentence one. " + // 24 chars
                          "This is sentence two. " + // 24 chars
                          "This is sentence three. " + // 27 chars
                          "This is sentence four. " + // 25 chars
                          "This is sentence five. " + // 25 chars
                          "This is sentence six. " + // 24 chars
                          "End of text."; // 12 chars

            // Act
            List<String> chunks = invokeChunkText(text);

            // Assert
            assertThat(chunks.size()).isGreaterThan(1);
        }

        @Test
        @DisplayName("should handle text exactly at chunk size")
        void shouldHandleTextExactlyAtChunkSize() {
            // Arrange - Create text with precise length
            StringBuilder sb = new StringBuilder();
            while (sb.length() < 100) {
                sb.append("x");
            }
            String exactText = sb.toString();

            // Act
            List<String> chunks = invokeChunkText(exactText);

            // Assert - Should produce at least one chunk
            assertThat(chunks).isNotEmpty();
        }

        @Test
        @DisplayName("should handle overlap between chunks")
        void shouldHandleOverlapBetweenChunks() {
            // Arrange - Long text with clear sentence boundaries
            String text = "First sentence. Second sentence. Third sentence. Fourth sentence. Fifth sentence. Sixth sentence.";

            // Act
            List<String> chunks = invokeChunkText(text);

            // Assert
            assertThat(chunks).isNotEmpty();
            // With overlap, consecutive chunks should share some content
            if (chunks.size() > 1) {
                boolean hasOverlap = false;
                for (int i = 1; i < chunks.size(); i++) {
                    String prevChunk = chunks.get(i - 1);
                    String currChunk = chunks.get(i);
                    // Check if current chunk starts with content from previous chunk
                    if (currChunk.contains(prevChunk.substring(Math.max(0, prevChunk.length() - 20)))) {
                        hasOverlap = true;
                        break;
                    }
                }
                assertThat(hasOverlap).isTrue();
            }
        }

        @Test
        @DisplayName("should handle single long sentence")
        void shouldHandleSingleLongSentence() {
            // Arrange - Single sentence longer than chunk size
            String longSentence = "This is a very long sentence that goes on and on and on without any punctuation marks to split on " +
                                  "and should therefore be treated as one continuous string of text that needs to be chunked based on size.";

            // Act
            List<String> chunks = invokeChunkText(longSentence);

            // Assert
            assertThat(chunks).isNotEmpty();
        }

        @Test
        @DisplayName("should handle sentences without spaces")
        void shouldHandleSentencesWithoutSpaces() {
            // Act
            List<String> chunks = invokeChunkText("Sentenceone.Sentencetwo.");

            // Assert
            assertThat(chunks).isNotEmpty();
        }

        @Test
        @DisplayName("should handle various sentence terminators")
        void shouldHandleVariousSentenceTerminators() {
            // Arrange
            String text = "Is this a question? Yes! Is this exciting? Absolutely! What about this.";

            // Act
            List<String> chunks = invokeChunkText(text);

            // Assert
            assertThat(chunks).isNotEmpty();
        }

        @Test
        @DisplayName("should handle Chinese punctuation")
        void shouldHandleChinesePunctuation() {
            // Arrange
            String text = "这是第一句。这是第二句！这是第三句？";

            // Act
            List<String> chunks = invokeChunkText(text);

            // Assert
            assertThat(chunks).isNotEmpty();
        }
    }

    @Nested
    @DisplayName("chunkText with different chunk sizes")
    class ChunkTextDifferentSizesTests {

        @Test
        @DisplayName("should create more chunks with smaller chunk size")
        void shouldCreateMoreChunksWithSmallerChunkSize() {
            // Arrange
            UploadDocumentUseCase smallChunkUseCase = new UploadDocumentUseCase(
                    documentRepository, embeddingPort, vectorSearchPort, 50, 10);
            UploadDocumentUseCase largeChunkUseCase = new UploadDocumentUseCase(
                    documentRepository, embeddingPort, vectorSearchPort, 200, 20);
            String text = "First sentence. Second sentence. Third sentence. Fourth sentence. Fifth sentence. Sixth sentence.";

            // Act
            List<String> smallChunks = invokeChunkTextOnUseCase(smallChunkUseCase, text);
            List<String> largeChunks = invokeChunkTextOnUseCase(largeChunkUseCase, text);

            // Assert
            assertThat(smallChunks.size()).isGreaterThanOrEqualTo(largeChunks.size());
        }

        @Test
        @DisplayName("should handle zero overlap")
        void shouldHandleZeroOverlap() {
            // Arrange
            UploadDocumentUseCase zeroOverlapUseCase = new UploadDocumentUseCase(
                    documentRepository, embeddingPort, vectorSearchPort, 50, 0);
            String text = "First sentence. Second sentence. Third sentence. Fourth sentence.";

            // Act
            List<String> chunks = invokeChunkTextOnUseCase(zeroOverlapUseCase, text);

            // Assert
            assertThat(chunks).isNotEmpty();
        }
    }

    /**
     * Helper method to invoke private chunkText method via reflection.
     */
    private List<String> invokeChunkText(String text) {
        return invokeChunkTextOnUseCase(useCase, text);
    }

    private List<String> invokeChunkTextOnUseCase(UploadDocumentUseCase useCase, String text) {
        try {
            var method = UploadDocumentUseCase.class.getDeclaredMethod("chunkText", String.class);
            method.setAccessible(true);
            @SuppressWarnings("unchecked")
            List<String> result = (List<String>) method.invoke(useCase, text);
            return result;
        } catch (Exception e) {
            throw new RuntimeException("Failed to invoke chunkText via reflection", e);
        }
    }
}
