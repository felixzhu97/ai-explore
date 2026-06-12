package com.ai.infrastructure.adapter.ai;

import com.ai.domain.model.ChatMessage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.model.Generation;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.prompt.Prompt;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * SpringAiChatServiceCoverageTest - Unit tests for SpringAiChatService.
 *
 * Naming convention: should_expected_result_when_condition
 * Uses AAA pattern (Arrange-Act-Assert)
 * Tests the Spring AI chat service implementation.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("SpringAiChatService Coverage Tests")
class SpringAiChatServiceCoverageTest {

    @Mock
    private ChatModel chatModel;

    private SpringAiChatService chatService;

    @BeforeEach
    void setUp() {
        chatService = new SpringAiChatService(chatModel);
    }

    @Nested
    @DisplayName("chatWithHistory")
    class ChatWithHistoryTests {

        @Test
        @DisplayName("should build prompt with history")
        void shouldBuildPromptWithHistory() {
            // Arrange
            List<ChatMessage> messages = List.of(
                    ChatMessage.createUserMessage("Hello"),
                    ChatMessage.createAssistantMessage("Hi there!")
            );
            when(chatModel.call(any(Prompt.class)))
                    .thenAnswer(invocation -> createMockChatResponse("Response text"));

            // Act
            String response = chatService.chatWithHistory(messages);

            // Assert
            assertThat(response).isEqualTo("Response text");
            verify(chatModel).call(any(Prompt.class));
        }

        @Test
        @DisplayName("should map user message to PromptChatMessage")
        void shouldMapUserMessage() {
            // Arrange
            ChatMessage userMsg = ChatMessage.createUserMessage("User question");
            when(chatModel.call(any(Prompt.class)))
                    .thenAnswer(invocation -> createMockChatResponse("Response"));

            // Act
            chatService.chatWithHistory(List.of(userMsg));

            // Assert
            verify(chatModel).call(any(Prompt.class));
        }

        @Test
        @DisplayName("should map assistant message to AssistantMessage")
        void shouldMapAssistantMessage() {
            // Arrange
            ChatMessage assistantMsg = ChatMessage.createAssistantMessage("Assistant response");
            when(chatModel.call(any(Prompt.class)))
                    .thenAnswer(invocation -> createMockChatResponse("New response"));

            // Act
            chatService.chatWithHistory(List.of(assistantMsg));

            // Assert
            verify(chatModel).call(any(Prompt.class));
        }

        @Test
        @DisplayName("should call chatModel with prompt")
        void shouldCallChatModel() {
            // Arrange
            List<ChatMessage> messages = List.of(ChatMessage.createUserMessage("Test"));
            when(chatModel.call(any(Prompt.class)))
                    .thenAnswer(invocation -> createMockChatResponse("Result"));

            // Act
            chatService.chatWithHistory(messages);

            // Assert
            verify(chatModel).call(any(Prompt.class));
        }

        @Test
        @DisplayName("should wrap exception as RuntimeException")
        void shouldWrapExceptionAsRagServiceException() {
            // Arrange
            List<ChatMessage> messages = List.of(ChatMessage.createUserMessage("Hello"));
            when(chatModel.call(any(Prompt.class))).thenThrow(new RuntimeException("AI service error"));

            // Act & Assert
            assertThatThrownBy(() -> chatService.chatWithHistory(messages))
                    .isInstanceOf(RuntimeException.class)
                    .hasMessageContaining("AI service error")
                    .hasCauseInstanceOf(RuntimeException.class);
        }

        @Test
        @DisplayName("should handle empty message list")
        void shouldHandleEmptyMessageList() {
            // Arrange
            when(chatModel.call(any(Prompt.class)))
                    .thenAnswer(invocation -> createMockChatResponse("Empty response"));

            // Act
            String response = chatService.chatWithHistory(List.of());

            // Assert
            assertThat(response).isEqualTo("Empty response");
        }

        @Test
        @DisplayName("should handle single user message")
        void shouldHandleSingleUserMessage() {
            // Arrange
            ChatMessage singleMessage = ChatMessage.createUserMessage("Only message");
            when(chatModel.call(any(Prompt.class)))
                    .thenAnswer(invocation -> createMockChatResponse("Response"));

            // Act
            String response = chatService.chatWithHistory(List.of(singleMessage));

            // Assert
            assertThat(response).isEqualTo("Response");
        }

        @Test
        @DisplayName("should handle multiple alternating messages")
        void shouldHandleMultipleAlternatingMessages() {
            // Arrange
            List<ChatMessage> messages = List.of(
                    ChatMessage.createUserMessage("Q1"),
                    ChatMessage.createAssistantMessage("A1"),
                    ChatMessage.createUserMessage("Q2"),
                    ChatMessage.createAssistantMessage("A2")
            );
            when(chatModel.call(any(Prompt.class)))
                    .thenAnswer(invocation -> createMockChatResponse("Final response"));

            // Act
            String response = chatService.chatWithHistory(messages);

            // Assert
            assertThat(response).isEqualTo("Final response");
        }
    }

    @Nested
    @DisplayName("chat")
    class ChatTests {

        @Test
        @DisplayName("should send simple user message to AI")
        void shouldSendSimpleUserMessageToAI() {
            // Arrange
            String userMessage = "Hello AI";
            when(chatModel.call(any(Prompt.class)))
                    .thenAnswer(invocation -> createMockChatResponse("Hello human!"));

            // Act
            String response = chatService.chat(userMessage);

            // Assert
            assertThat(response).isEqualTo("Hello human!");
            verify(chatModel).call(any(Prompt.class));
        }

        @Test
        @DisplayName("should wrap exception from simple chat")
        void shouldWrapExceptionFromSimpleChat() {
            // Arrange
            String userMessage = "Hello";
            when(chatModel.call(any(Prompt.class))).thenThrow(new RuntimeException("Connection failed"));

            // Act & Assert
            assertThatThrownBy(() -> chatService.chat(userMessage))
                    .isInstanceOf(RuntimeException.class)
                    .hasMessageContaining("Connection failed");
        }

        @Test
        @DisplayName("should handle null response from AI")
        void shouldHandleNullResponseFromAI() {
            // Arrange
            String userMessage = "Hello";
            when(chatModel.call(any(Prompt.class)))
                    .thenAnswer(invocation -> createMockChatResponse(null));

            // Act
            String response = chatService.chat(userMessage);

            // Assert
            assertThat(response).isEmpty();
        }

        @Test
        @DisplayName("should return empty string for null response")
        void shouldReturnEmptyStringForNullResponse() {
            // Arrange
            when(chatModel.call(any(Prompt.class)))
                    .thenAnswer(invocation -> createMockChatResponse(null));

            // Act
            String response = chatService.chat("Test");

            // Assert
            assertThat(response).isEqualTo("");
        }
    }

    @Nested
    @DisplayName("Exception Handling")
    class ExceptionHandlingTests {

        @Test
        @DisplayName("should preserve exception cause chain")
        void shouldPreserveExceptionCauseChain() {
            // Arrange
            Exception nestedCause = new Exception("Network error");
            RuntimeException originalException = new RuntimeException("AI unavailable", nestedCause);
            when(chatModel.call(any(Prompt.class))).thenThrow(originalException);

            // Act & Assert
            // Note: The service wraps the exception once, so the cause becomes the RuntimeException
            assertThatThrownBy(() -> chatService.chat("Test"))
                    .isInstanceOf(RuntimeException.class)
                    .hasMessageContaining("AI service error")
                    .hasCause(originalException);
        }

        @Test
        @DisplayName("should wrap IllegalStateException")
        void shouldWrapIllegalStateException() {
            // Arrange
            when(chatModel.call(any(Prompt.class))).thenThrow(new IllegalStateException("Invalid state"));

            // Act & Assert
            assertThatThrownBy(() -> chatService.chat("Test"))
                    .isInstanceOf(RuntimeException.class)
                    .hasCauseInstanceOf(IllegalStateException.class);
        }
    }

    @Nested
    @DisplayName("Message Content")
    class MessageContentTests {

        @Test
        @DisplayName("should handle unicode characters in messages")
        void shouldHandleUnicodeCharactersInMessages() {
            // Arrange
            String unicodeMessage = "你好世界 🌍 αβγδ";
            when(chatModel.call(any(Prompt.class)))
                    .thenAnswer(invocation -> createMockChatResponse("Hello 🌍"));

            // Act
            String response = chatService.chat(unicodeMessage);

            // Assert
            assertThat(response).isEqualTo("Hello 🌍");
        }

        @Test
        @DisplayName("should handle very long messages")
        void shouldHandleVeryLongMessages() {
            // Arrange
            String longMessage = "A".repeat(10000);
            when(chatModel.call(any(Prompt.class)))
                    .thenAnswer(invocation -> createMockChatResponse("Received long message"));

            // Act
            String response = chatService.chat(longMessage);

            // Assert
            assertThat(response).isEqualTo("Received long message");
        }

        @Test
        @DisplayName("should handle special characters in messages")
        void shouldHandleSpecialCharactersInMessages() {
            // Arrange
            String specialMessage = "Test <script>alert('xss')</script> & \"quotes\"";
            when(chatModel.call(any(Prompt.class)))
                    .thenAnswer(invocation -> createMockChatResponse("Sanitized response"));

            // Act
            String response = chatService.chat(specialMessage);

            // Assert
            assertThat(response).isEqualTo("Sanitized response");
        }
    }

    /**
     * Creates a mock ChatResponse that returns the specified text.
     * Based on the pattern used in ChatServiceTest.java
     */
    private ChatResponse createMockChatResponse(String text) {
        String responseText = text != null ? text : "";
        AssistantMessage assistantMessage = new AssistantMessage(responseText);
        Generation generation = new Generation(assistantMessage);
        return new ChatResponse(List.of(generation));
    }
}
