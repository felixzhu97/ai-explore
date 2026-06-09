package com.ai.tts.domain;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("ProviderInfo Tests")
class ProviderInfoTest {

    @Nested
    @DisplayName("of factory method")
    class OfFactoryMethodTests {

        @Test
        @DisplayName("should create provider info with all parameters")
        void shouldCreateProviderInfoWithAllParameters() {
            List<String> languages = List.of("en-US", "zh-CN");
            List<String> features = List.of("streaming", "voice-cloning");

            ProviderInfo info = ProviderInfo.of("test-provider", "Test Provider", languages, features);

            assertThat(info.name()).isEqualTo("test-provider");
            assertThat(info.displayName()).isEqualTo("Test Provider");
            assertThat(info.supportedLanguages()).containsExactly("en-US", "zh-CN");
            assertThat(info.features()).containsExactly("streaming", "voice-cloning");
        }

        @Test
        @DisplayName("should set isActive to false by default")
        void shouldSetIsActiveToFalseByDefault() {
            ProviderInfo info = ProviderInfo.of("test", "Test", List.of(), List.of());

            assertThat(info.isActive()).isFalse();
        }
    }

    @Nested
    @DisplayName("active factory method")
    class ActiveFactoryMethodTests {

        @Test
        @DisplayName("should create active provider info")
        void shouldCreateActiveProviderInfo() {
            ProviderInfo info = ProviderInfo.active("active-provider", "Active Provider", List.of("en"), List.of());

            assertThat(info.isActive()).isTrue();
        }

        @Test
        @DisplayName("should preserve other properties when active")
        void shouldPreserveOtherPropertiesWhenActive() {
            ProviderInfo info = ProviderInfo.active("test", "Test", List.of("zh-CN"), List.of("feature1"));

            assertThat(info.name()).isEqualTo("test");
            assertThat(info.displayName()).isEqualTo("Test");
            assertThat(info.supportedLanguages()).contains("zh-CN");
            assertThat(info.features()).contains("feature1");
        }
    }

    @Nested
    @DisplayName("record accessors")
    class RecordAccessorTests {

        @Test
        @DisplayName("should provide all accessors")
        void shouldProvideAllAccessors() {
            List<String> languages = List.of("en-US");
            List<String> features = List.of("streaming");
            ProviderInfo info = ProviderInfo.of("provider", "Display", languages, features);

            assertThat(info.name()).isEqualTo("provider");
            assertThat(info.displayName()).isEqualTo("Display");
            assertThat(info.supportedLanguages()).isEqualTo(languages);
            assertThat(info.features()).isEqualTo(features);
            assertThat(info.isActive()).isFalse();
        }
    }
}
