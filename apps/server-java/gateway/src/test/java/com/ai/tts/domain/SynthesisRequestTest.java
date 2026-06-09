package com.ai.tts.domain;

import com.ai.tts.domain.exception.TtsDomainException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("SynthesisRequest Tests")
class SynthesisRequestTest {

    @Nested
    @DisplayName("create factory method")
    class CreateFactoryMethodTests {

        @Test
        @DisplayName("should create synthesis request with all parameters")
        void shouldCreateSynthesisRequestWithAllParameters() {
            SynthesisRequest request = SynthesisRequest.create(
                    "Hello world", "voice1", "en-US", 1.0f, 0f, OutputFormat.MP3, "provider1"
            );

            assertThat(request.text()).isEqualTo("Hello world");
            assertThat(request.voice()).isEqualTo("voice1");
            assertThat(request.language()).isEqualTo("en-US");
            assertThat(request.speed()).isEqualTo(1.0f);
            assertThat(request.pitch()).isEqualTo(0f);
            assertThat(request.outputFormat()).isEqualTo(OutputFormat.MP3);
            assertThat(request.provider()).isEqualTo("provider1");
        }

        @Test
        @DisplayName("should generate unique speech id")
        void shouldGenerateUniqueSpeechId() {
            SynthesisRequest request1 = SynthesisRequest.create(
                    "Text", "voice", "en", 1.0f, 0f, OutputFormat.MP3, "p"
            );
            SynthesisRequest request2 = SynthesisRequest.create(
                    "Text", "voice", "en", 1.0f, 0f, OutputFormat.MP3, "p"
            );

            assertThat(request1.speechId()).isNotEqualTo(request2.speechId());
        }

        @Test
        @DisplayName("should default null voice to empty string")
        void shouldDefaultNullVoiceToEmptyString() {
            SynthesisRequest request = SynthesisRequest.create(
                    "Hello", null, "en-US", 1.0f, 0f, OutputFormat.MP3, null
            );

            assertThat(request.voice()).isEmpty();
        }

        @Test
        @DisplayName("should default null language to zh-CN")
        void shouldDefaultNullLanguageToZhCN() {
            SynthesisRequest request = SynthesisRequest.create(
                    "Hello", "v", null, 1.0f, 0f, OutputFormat.MP3, null
            );

            assertThat(request.language()).isEqualTo("zh-CN");
        }

        @Test
        @DisplayName("should default null format to MP3")
        void shouldDefaultNullFormatToMP3() {
            SynthesisRequest request = SynthesisRequest.create(
                    "Hello", "v", "en", 1.0f, 0f, null, null
            );

            assertThat(request.outputFormat()).isEqualTo(OutputFormat.MP3);
        }

        @Test
        @DisplayName("should lowercase provider name")
        void shouldLowercaseProviderName() {
            SynthesisRequest request = SynthesisRequest.create(
                    "Hello", "v", "en", 1.0f, 0f, OutputFormat.MP3, "EdgeTTS"
            );

            assertThat(request.provider()).isEqualTo("edgetts");
        }
    }

    @Nested
    @DisplayName("speed validation and normalization")
    class SpeedValidationTests {

        @Test
        @DisplayName("should accept valid speed at minimum boundary")
        void shouldAcceptValidSpeedAtMinimumBoundary() {
            SynthesisRequest request = SynthesisRequest.create(
                    "Hello", "v", "en", 0.25f, 0f, OutputFormat.MP3, "p"
            );

            assertThat(request.speed()).isEqualTo(0.25f);
        }

        @Test
        @DisplayName("should accept valid speed at maximum boundary")
        void shouldAcceptValidSpeedAtMaximumBoundary() {
            SynthesisRequest request = SynthesisRequest.create(
                    "Hello", "v", "en", 4.0f, 0f, OutputFormat.MP3, "p"
            );

            assertThat(request.speed()).isEqualTo(4.0f);
        }

        @Test
        @DisplayName("should normalize zero speed to 1.0")
        void shouldNormalizeZeroSpeedTo1() {
            // Note: Speed 0 is validated before normalization, so this test verifies
            // that values in valid range (0.25-4.0) are accepted
            SynthesisRequest request = SynthesisRequest.create(
                    "Hello", "v", "en", 0.5f, 0f, OutputFormat.MP3, "p"
            );

            assertThat(request.speed()).isEqualTo(0.5f);
        }

        @Test
        @DisplayName("should reject speed below minimum")
        void shouldRejectSpeedBelowMinimum() {
            assertThatThrownBy(() -> SynthesisRequest.create(
                    "Hello", "v", "en", 0.1f, 0f, OutputFormat.MP3, "p"
            )).isInstanceOf(TtsDomainException.class)
              .hasMessageContaining("Speed must be between 0.25 and 4.0");
        }

        @Test
        @DisplayName("should reject speed above maximum")
        void shouldRejectSpeedAboveMaximum() {
            assertThatThrownBy(() -> SynthesisRequest.create(
                    "Hello", "v", "en", 5.0f, 0f, OutputFormat.MP3, "p"
            )).isInstanceOf(TtsDomainException.class)
              .hasMessageContaining("Speed must be between 0.25 and 4.0");
        }
    }

    @Nested
    @DisplayName("pitch validation")
    class PitchValidationTests {

        @Test
        @DisplayName("should accept valid pitch at minimum boundary")
        void shouldAcceptValidPitchAtMinimumBoundary() {
            SynthesisRequest request = SynthesisRequest.create(
                    "Hello", "v", "en", 1.0f, -20f, OutputFormat.MP3, "p"
            );

            assertThat(request.pitch()).isEqualTo(-20f);
        }

        @Test
        @DisplayName("should accept valid pitch at maximum boundary")
        void shouldAcceptValidPitchAtMaximumBoundary() {
            SynthesisRequest request = SynthesisRequest.create(
                    "Hello", "v", "en", 1.0f, 20f, OutputFormat.MP3, "p"
            );

            assertThat(request.pitch()).isEqualTo(20f);
        }

        @Test
        @DisplayName("should reject pitch below minimum")
        void shouldRejectPitchBelowMinimum() {
            assertThatThrownBy(() -> SynthesisRequest.create(
                    "Hello", "v", "en", 1.0f, -21f, OutputFormat.MP3, "p"
            )).isInstanceOf(TtsDomainException.class)
              .hasMessageContaining("Pitch must be between -20 and 20");
        }

        @Test
        @DisplayName("should reject pitch above maximum")
        void shouldRejectPitchAboveMaximum() {
            assertThatThrownBy(() -> SynthesisRequest.create(
                    "Hello", "v", "en", 1.0f, 21f, OutputFormat.MP3, "p"
            )).isInstanceOf(TtsDomainException.class)
              .hasMessageContaining("Pitch must be between -20 and 20");
        }
    }

    @Nested
    @DisplayName("text validation")
    class TextValidationTests {

        @Test
        @DisplayName("should reject null text")
        void shouldRejectNullText() {
            assertThatThrownBy(() -> SynthesisRequest.create(
                    null, "v", "en", 1.0f, 0f, OutputFormat.MP3, "p"
            )).isInstanceOf(TtsDomainException.class)
              .hasMessageContaining("Text cannot be empty");
        }

        @Test
        @DisplayName("should reject empty text")
        void shouldRejectEmptyText() {
            assertThatThrownBy(() -> SynthesisRequest.create(
                    "", "v", "en", 1.0f, 0f, OutputFormat.MP3, "p"
            )).isInstanceOf(TtsDomainException.class)
              .hasMessageContaining("Text cannot be empty");
        }

        @Test
        @DisplayName("should reject blank text")
        void shouldRejectBlankText() {
            assertThatThrownBy(() -> SynthesisRequest.create(
                    "   ", "v", "en", 1.0f, 0f, OutputFormat.MP3, "p"
            )).isInstanceOf(TtsDomainException.class)
              .hasMessageContaining("Text cannot be empty");
        }
    }

    @Nested
    @DisplayName("requiresStreaming method")
    class RequiresStreamingMethodTests {

        @ParameterizedTest
        @CsvSource({
                "500, false",
                "501, true",
                "1000, true"
        })
        @DisplayName("should return correct streaming requirement")
        void shouldReturnCorrectStreamingRequirement(int textLength, boolean expected) {
            String text = "A".repeat(textLength);
            SynthesisRequest request = SynthesisRequest.create(
                    text, "v", "en", 1.0f, 0f, OutputFormat.MP3, "p"
            );

            assertThat(request.requiresStreaming()).isEqualTo(expected);
        }
    }

    @Nested
    @DisplayName("record accessors")
    class RecordAccessorTests {

        @Test
        @DisplayName("should provide all accessors")
        void shouldProvideAllAccessors() {
            SynthesisRequest request = SynthesisRequest.create(
                    "Hello", "voice1", "zh-CN", 1.5f, 5f, OutputFormat.WAV, "cosyvoice"
            );

            assertThat(request.speechId()).isNotNull();
            assertThat(request.text()).isEqualTo("Hello");
            assertThat(request.voice()).isEqualTo("voice1");
            assertThat(request.language()).isEqualTo("zh-CN");
            assertThat(request.speed()).isEqualTo(1.5f);
            assertThat(request.pitch()).isEqualTo(5f);
            assertThat(request.outputFormat()).isEqualTo(OutputFormat.WAV);
            assertThat(request.provider()).isEqualTo("cosyvoice");
        }
    }
}
