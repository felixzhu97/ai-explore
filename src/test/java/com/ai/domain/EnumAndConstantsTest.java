package com.ai.domain;

import com.ai.ai.domain.model.ChatMessageType;
import com.ai.ai.domain.model.ChatSessionStatus;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Enum and Constants Tests
 *
 * Tests for domain enums.
 */
@DisplayName("Enum and Constants Tests")
class EnumAndConstantsTest {

    @Nested
    @DisplayName("ChatMessageType enum tests")
    class ChatMessageTypeTests {

        @Test
        @DisplayName("should have USER and ASSISTANT values")
        void shouldHaveUserAndAssistantValues() {
            // Assert
            assertThat(ChatMessageType.values())
                    .containsExactly(ChatMessageType.USER, ChatMessageType.ASSISTANT);
        }

        @Test
        @DisplayName("should have exactly 2 values")
        void shouldHaveExactly2Values() {
            // Assert
            assertThat(ChatMessageType.values()).hasSize(2);
        }

        @Test
        @DisplayName("should get value by name")
        void shouldGetValueByName() {
            // Act & Assert
            assertThat(ChatMessageType.valueOf("USER")).isEqualTo(ChatMessageType.USER);
            assertThat(ChatMessageType.valueOf("ASSISTANT")).isEqualTo(ChatMessageType.ASSISTANT);
        }

        @Test
        @DisplayName("USER should have correct name")
        void userShouldHaveCorrectName() {
            // Assert
            assertThat(ChatMessageType.USER.name()).isEqualTo("USER");
        }

        @Test
        @DisplayName("ASSISTANT should have correct name")
        void assistantShouldHaveCorrectName() {
            // Assert
            assertThat(ChatMessageType.ASSISTANT.name()).isEqualTo("ASSISTANT");
        }
    }

    @Nested
    @DisplayName("ChatSessionStatus enum tests")
    class ChatSessionStatusTests {

        @Test
        @DisplayName("should have ACTIVE and CLOSED values")
        void shouldHaveActiveAndClosedValues() {
            // Assert
            assertThat(ChatSessionStatus.values())
                    .containsExactly(ChatSessionStatus.ACTIVE, ChatSessionStatus.CLOSED);
        }

        @Test
        @DisplayName("should have exactly 2 values")
        void shouldHaveExactly2Values() {
            // Assert
            assertThat(ChatSessionStatus.values()).hasSize(2);
        }

        @Test
        @DisplayName("should get value by name")
        void shouldGetValueByName() {
            // Act & Assert
            assertThat(ChatSessionStatus.valueOf("ACTIVE")).isEqualTo(ChatSessionStatus.ACTIVE);
            assertThat(ChatSessionStatus.valueOf("CLOSED")).isEqualTo(ChatSessionStatus.CLOSED);
        }

        @Test
        @DisplayName("ACTIVE should have correct name")
        void activeShouldHaveCorrectName() {
            // Assert
            assertThat(ChatSessionStatus.ACTIVE.name()).isEqualTo("ACTIVE");
        }

        @Test
        @DisplayName("CLOSED should have correct name")
        void closedShouldHaveCorrectName() {
            // Assert
            assertThat(ChatSessionStatus.CLOSED.name()).isEqualTo("CLOSED");
        }
    }
}
