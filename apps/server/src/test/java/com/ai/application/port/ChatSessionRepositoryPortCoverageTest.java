package com.ai.application.port;

import com.ai.domain.model.ChatSession;
import com.ai.domain.vo.ChatSessionId;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * ChatSessionRepositoryPortCoverageTest - Unit tests for ChatSessionRepositoryPort.getOrCreateDefaultSession().
 *
 * Naming convention: should_expected_result_when_condition
 * Uses AAA pattern (Arrange-Act-Assert)
 * Tests the default implementation of getOrCreateDefaultSession.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("ChatSessionRepositoryPort Coverage Tests")
class ChatSessionRepositoryPortCoverageTest {

    @Mock
    private ChatSessionRepositoryPort repositoryPort;

    private TestableChatSessionRepositoryPort testPort;

    @BeforeEach
    void setUp() {
        // Create a testable wrapper that uses the mock
        testPort = new TestableChatSessionRepositoryPort(repositoryPort);
    }

    @Nested
    @DisplayName("getOrCreateDefaultSession")
    class GetOrCreateDefaultSessionTests {

        @Test
        @DisplayName("should return existing session when exists")
        void shouldReturnExistingSessionWhenExists() {
            // Arrange
            ChatSession existingSession = ChatSession.create("Existing Chat");
            setSessionId(existingSession, "existing-session-id");
            when(repositoryPort.findAll()).thenReturn(List.of(existingSession));

            // Act
            ChatSession result = testPort.getOrCreateDefaultSession();

            // Assert
            assertThat(result).isEqualTo(existingSession);
            verify(repositoryPort).findAll();
            verify(repositoryPort, never()).save(any(ChatSession.class));
        }

        @Test
        @DisplayName("should create new session when no sessions exist")
        void shouldCreateNewSessionWhenNotExists() {
            // Arrange
            when(repositoryPort.findAll()).thenReturn(List.of());

            // Act
            ChatSession result = testPort.getOrCreateDefaultSession();

            // Assert
            assertThat(result).isNotNull();
            assertThat(result.getTitle()).isEqualTo("Default Chat");
            verify(repositoryPort).findAll();
            verify(repositoryPort).save(any(ChatSession.class));
        }

        @Test
        @DisplayName("should return first session when multiple exist")
        void shouldReturnFirstSessionWhenMultipleExist() {
            // Arrange
            ChatSession session1 = ChatSession.create("First Session");
            setSessionId(session1, "first-id");
            ChatSession session2 = ChatSession.create("Second Session");
            setSessionId(session2, "second-id");
            ChatSession session3 = ChatSession.create("Third Session");
            setSessionId(session3, "third-id");

            when(repositoryPort.findAll()).thenReturn(List.of(session1, session2, session3));

            // Act
            ChatSession result = testPort.getOrCreateDefaultSession();

            // Assert
            assertThat(result).isEqualTo(session1);
            verify(repositoryPort).findAll();
            verify(repositoryPort, never()).save(any(ChatSession.class));
        }

        @Test
        @DisplayName("should return empty session list result")
        void shouldReturnEmptySessionListResult() {
            // Arrange
            when(repositoryPort.findAll()).thenReturn(List.of());

            // Act
            ChatSession result = testPort.getOrCreateDefaultSession();

            // Assert
            assertThat(result).isNotNull();
            assertThat(result.getId()).isNotNull();
            verify(repositoryPort).save(any(ChatSession.class));
        }

        @Test
        @DisplayName("should handle empty session list")
        void shouldHandleEmptySessionList() {
            // Arrange
            when(repositoryPort.findAll()).thenReturn(List.of());

            // Act
            ChatSession result = testPort.getOrCreateDefaultSession();

            // Assert
            assertThat(result).isNotNull();
            assertThat(result.getTitle()).isEqualTo("Default Chat");
            verify(repositoryPort).save(any(ChatSession.class));
        }

        @Test
        @DisplayName("should handle single session in list")
        void shouldHandleSingleSessionInList() {
            // Arrange
            ChatSession singleSession = ChatSession.create("Only Session");
            setSessionId(singleSession, "single-id");
            when(repositoryPort.findAll()).thenReturn(List.of(singleSession));

            // Act
            ChatSession result = testPort.getOrCreateDefaultSession();

            // Assert
            assertThat(result).isEqualTo(singleSession);
            verify(repositoryPort).findAll();
            verify(repositoryPort, never()).save(any(ChatSession.class));
        }
    }

    @Nested
    @DisplayName("getOrCreateDefaultSession title handling")
    class TitleHandlingTests {

        @Test
        @DisplayName("should create session with correct default title")
        void shouldCreateSessionWithCorrectDefaultTitle() {
            // Arrange
            when(repositoryPort.findAll()).thenReturn(List.of());

            // Act
            ChatSession result = testPort.getOrCreateDefaultSession();

            // Assert
            assertThat(result.getTitle()).isEqualTo("Default Chat");
        }
    }

    /**
     * Helper class to test the default implementation of ChatSessionRepositoryPort.
     * Uses composition to access the default method implementation.
     */
    private static class TestableChatSessionRepositoryPort implements ChatSessionRepositoryPort {
        private final ChatSessionRepositoryPort delegate;

        TestableChatSessionRepositoryPort(ChatSessionRepositoryPort delegate) {
            this.delegate = delegate;
        }

        @Override
        public Optional<ChatSession> findById(ChatSessionId id) {
            return delegate.findById(id);
        }

        @Override
        public void save(ChatSession session) {
            delegate.save(session);
        }

        @Override
        public void delete(ChatSessionId id) {
            delegate.delete(id);
        }

        @Override
        public List<ChatSession> findAll() {
            return delegate.findAll();
        }

        @Override
        public boolean exists(ChatSessionId id) {
            return delegate.exists(id);
        }

        @Override
        public ChatSession getOrCreateDefaultSession() {
            // Delegate to the default implementation
            return ChatSessionRepositoryPort.super.getOrCreateDefaultSession();
        }
    }

    /**
     * Helper method to set session ID via reflection for testing.
     */
    private void setSessionId(ChatSession session, String id) {
        try {
            var idField = ChatSession.class.getDeclaredField("id");
            idField.setAccessible(true);
            idField.set(session, ChatSessionId.of(id));
        } catch (Exception e) {
            throw new RuntimeException("Failed to set session ID via reflection", e);
        }
    }
}
