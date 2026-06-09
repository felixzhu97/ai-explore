package com.ai.tts.domain;

import com.ai.tts.domain.exception.TtsDomainException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("Speech Tests")
class SpeechTest {

    @Nested
    @DisplayName("create factory method")
    class CreateFactoryMethodTests {

        @Test
        @DisplayName("should create speech with PENDING status")
        void shouldCreateSpeechWithPendingStatus() {
            SynthesisRequest request = SynthesisRequest.create("Hello", "voice1", "en-US", 1.0f, 1.0f, OutputFormat.MP3, "test");

            Speech speech = Speech.create(request);

            assertThat(speech.status()).isEqualTo(Speech.SpeechStatus.PENDING);
        }

        @Test
        @DisplayName("should inherit request from synthesis request")
        void shouldInheritRequestFromSynthesisRequest() {
            SynthesisRequest request = SynthesisRequest.create("Hello world", "voice1", "en-US", 1.0f, 1.0f, OutputFormat.MP3, "test");

            Speech speech = Speech.create(request);

            assertThat(speech.text()).isEqualTo("Hello world");
            assertThat(speech.voice()).isEqualTo("voice1");
        }
    }

    @Nested
    @DisplayName("fromRequest factory method")
    class FromRequestFactoryMethodTests {

        @Test
        @DisplayName("should create speech with all parameters")
        void shouldCreateSpeechWithAllParameters() {
            Speech speech = Speech.fromRequest("Test text", "voice1", "en-US", 1.0f, 1.0f, OutputFormat.WAV, "provider1");

            assertThat(speech.text()).isEqualTo("Test text");
            assertThat(speech.voice()).isEqualTo("voice1");
            assertThat(speech.language()).isEqualTo("en-US");
            assertThat(speech.speed()).isEqualTo(1.0f);
            assertThat(speech.pitch()).isEqualTo(1.0f);
            assertThat(speech.outputFormat()).isEqualTo(OutputFormat.WAV);
            assertThat(speech.provider()).isEqualTo("provider1");
        }
    }

    @Nested
    @DisplayName("startSynthesis method")
    class StartSynthesisMethodTests {

        @Test
        @DisplayName("should transition to SYNTHESIZING status")
        void shouldTransitionToSynthesizingStatus() {
            SynthesisRequest request = SynthesisRequest.create("Hello", "voice1", "en-US", 1.0f, 1.0f, OutputFormat.MP3, "test");
            Speech speech = Speech.create(request);

            speech.startSynthesis();

            assertThat(speech.status()).isEqualTo(Speech.SpeechStatus.SYNTHESIZING);
        }

        @Test
        @DisplayName("should throw exception when not in PENDING status")
        void shouldThrowExceptionWhenNotInPendingStatus() {
            SynthesisRequest request = SynthesisRequest.create("Hello", "voice1", "en-US", 1.0f, 1.0f, OutputFormat.MP3, "test");
            Speech speech = Speech.create(request);
            speech.startSynthesis();

            assertThatThrownBy(() -> speech.startSynthesis())
                    .isInstanceOf(TtsDomainException.class);
        }
    }

    @Nested
    @DisplayName("complete method")
    class CompleteMethodTests {

        @Test
        @DisplayName("should transition to COMPLETED status with audio data")
        void shouldTransitionToCompletedStatusWithAudioData() {
            SynthesisRequest request = SynthesisRequest.create("Hello", "voice1", "en-US", 1.0f, 1.0f, OutputFormat.MP3, "test");
            Speech speech = Speech.create(request);
            speech.startSynthesis();
            byte[] audioData = new byte[]{1, 2, 3, 4};

            speech.complete(audioData);

            assertThat(speech.status()).isEqualTo(Speech.SpeechStatus.COMPLETED);
            assertThat(speech.isCompleted()).isTrue();
            assertThat(speech.result()).isNotNull();
        }

        @Test
        @DisplayName("should throw exception when not in SYNTHESIZING status")
        void shouldThrowExceptionWhenNotInSynthesizingStatus() {
            SynthesisRequest request = SynthesisRequest.create("Hello", "voice1", "en-US", 1.0f, 1.0f, OutputFormat.MP3, "test");
            Speech speech = Speech.create(request);

            assertThatThrownBy(() -> speech.complete(new byte[]{1, 2}))
                    .isInstanceOf(TtsDomainException.class);
        }
    }

    @Nested
    @DisplayName("fail method")
    class FailMethodTests {

        @Test
        @DisplayName("should transition to FAILED status")
        void shouldTransitionToFailedStatus() {
            SynthesisRequest request = SynthesisRequest.create("Hello", "voice1", "en-US", 1.0f, 1.0f, OutputFormat.MP3, "test");
            Speech speech = Speech.create(request);

            try {
                speech.fail(new RuntimeException("Synthesis failed"));
            } catch (TtsDomainException e) {
                // Expected
            }

            assertThat(speech.isFailed()).isTrue();
        }

        @Test
        @DisplayName("should throw exception with error message")
        void shouldThrowExceptionWithErrorMessage() {
            SynthesisRequest request = SynthesisRequest.create("Hello", "voice1", "en-US", 1.0f, 1.0f, OutputFormat.MP3, "test");
            Speech speech = Speech.create(request);

            assertThatThrownBy(() -> speech.fail(new RuntimeException("Network error")))
                    .isInstanceOf(TtsDomainException.class)
                    .hasMessageContaining("Network error");
        }
    }

    @Nested
    @DisplayName("canStream method")
    class CanStreamMethodTests {

        @Test
        @DisplayName("should return true for long text")
        void shouldReturnTrueForLongText() {
            String longText = "A".repeat(501);

            Speech speech = Speech.fromRequest(longText, "voice1", "en-US", 1.0f, 1.0f, OutputFormat.MP3, "test");

            assertThat(speech.canStream()).isTrue();
        }

        @Test
        @DisplayName("should return false for short text")
        void shouldReturnFalseForShortText() {
            Speech speech = Speech.fromRequest("Hello", "voice1", "en-US", 1.0f, 1.0f, OutputFormat.MP3, "test");

            assertThat(speech.canStream()).isFalse();
        }
    }

    @Nested
    @DisplayName("equals and hashCode")
    class EqualsAndHashCodeTests {

        @Test
        @DisplayName("should be equal for same id")
        void shouldBeEqualForSameId() {
            SynthesisRequest request = SynthesisRequest.create("Hello", "voice1", "en-US", 1.0f, 1.0f, OutputFormat.MP3, "test");
            Speech speech1 = Speech.create(request);
            Speech speech2 = Speech.create(request);

            assertThat(speech1).isEqualTo(speech2);
            assertThat(speech1.hashCode()).isEqualTo(speech2.hashCode());
        }

        @Test
        @DisplayName("should not be equal for different requests")
        void shouldNotBeEqualForDifferentRequests() {
            SynthesisRequest request1 = SynthesisRequest.create("Hello", "voice1", "en-US", 1.0f, 1.0f, OutputFormat.MP3, "test");
            SynthesisRequest request2 = SynthesisRequest.create("World", "voice2", "en-US", 1.0f, 1.0f, OutputFormat.MP3, "test");
            Speech speech1 = Speech.create(request1);
            Speech speech2 = Speech.create(request2);

            assertThat(speech1).isNotEqualTo(speech2);
        }
    }

    @Nested
    @DisplayName("SpeechStatus enum values")
    class SpeechStatusEnumTests {

        @Test
        @DisplayName("should have all expected status values")
        void shouldHaveAllExpectedStatusValues() {
            assertThat(Speech.SpeechStatus.values()).containsExactlyInAnyOrder(
                    Speech.SpeechStatus.PENDING,
                    Speech.SpeechStatus.SYNTHESIZING,
                    Speech.SpeechStatus.COMPLETED,
                    Speech.SpeechStatus.FAILED
            );
        }
    }
}
