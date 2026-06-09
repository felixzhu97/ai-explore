package com.ai.tts.domain;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("Voice Tests")
class VoiceTest {

    @Nested
    @DisplayName("of factory method")
    class OfFactoryMethodTests {

        @Test
        @DisplayName("should create voice with all parameters")
        void shouldCreateVoiceWithAllParameters() {
            Voice voice = Voice.of("voice-001", "Beijing Mandarin", "zh-CN", "provider1");

            assertThat(voice.id()).isEqualTo("voice-001");
            assertThat(voice.name()).isEqualTo("Beijing Mandarin");
            assertThat(voice.language()).isEqualTo("zh-CN");
            assertThat(voice.provider()).isEqualTo("provider1");
        }

        @Test
        @DisplayName("should set isDefault to false")
        void shouldSetIsDefaultToFalse() {
            Voice voice = Voice.of("voice-001", "Test", "en-US", "provider");

            assertThat(voice.isDefault()).isFalse();
        }

        @Test
        @DisplayName("should set languageName and gender to null")
        void shouldSetLanguageNameAndGenderToNull() {
            Voice voice = Voice.of("voice-001", "Test", "en-US", "provider");

            assertThat(voice.languageName()).isNull();
            assertThat(voice.gender()).isNull();
        }
    }

    @Nested
    @DisplayName("defaultVoice factory method")
    class DefaultVoiceFactoryMethodTests {

        @Test
        @DisplayName("should create default voice")
        void shouldCreateDefaultVoice() {
            Voice voice = Voice.defaultVoice("voice-001", "Default Voice", "en-US", "provider1");

            assertThat(voice.isDefault()).isTrue();
        }

        @Test
        @DisplayName("should preserve other properties")
        void shouldPreserveOtherProperties() {
            Voice voice = Voice.defaultVoice("voice-001", "Default Voice", "zh-CN", "provider1");

            assertThat(voice.id()).isEqualTo("voice-001");
            assertThat(voice.name()).isEqualTo("Default Voice");
            assertThat(voice.language()).isEqualTo("zh-CN");
            assertThat(voice.provider()).isEqualTo("provider1");
        }
    }

    @Nested
    @DisplayName("record accessors")
    class RecordAccessorTests {

        @Test
        @DisplayName("should provide all accessors")
        void shouldProvideAllAccessors() {
            Voice voice = Voice.of("id-123", "Test Voice", "en-US", "provider");

            assertThat(voice.id()).isEqualTo("id-123");
            assertThat(voice.name()).isEqualTo("Test Voice");
            assertThat(voice.language()).isEqualTo("en-US");
            assertThat(voice.languageName()).isNull();
            assertThat(voice.gender()).isNull();
            assertThat(voice.provider()).isEqualTo("provider");
            assertThat(voice.isDefault()).isFalse();
        }
    }

    @Nested
    @DisplayName("immutability")
    class ImmutabilityTests {

        @Test
        @DisplayName("should be immutable record")
        void shouldBeImmutableRecord() {
            Voice voice = Voice.of("id", "Test", "en", "provider");

            assertThat(voice.id()).isEqualTo("id");
            assertThat(voice).isInstanceOf(Voice.class);
        }
    }
}
