package com.ai.tts.domain;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("AudioResult Tests")
class AudioResultTest {

    @Nested
    @DisplayName("of factory method")
    class OfFactoryMethodTests {

        @Test
        @DisplayName("should create audio result with all parameters")
        void shouldCreateAudioResultWithAllParameters() {
            SpeechId speechId = SpeechId.generate();
            byte[] audioData = new byte[]{1, 2, 3, 4};

            AudioResult result = AudioResult.of(speechId, audioData, OutputFormat.MP3);

            assertThat(result.speechId()).isEqualTo(speechId);
            assertThat(result.audioData()).isEqualTo(audioData);
            assertThat(result.format()).isEqualTo(OutputFormat.MP3);
        }

        @Test
        @DisplayName("should set timestamp to current time")
        void shouldSetTimestampToCurrentTime() {
            SpeechId speechId = SpeechId.generate();
            long before = System.currentTimeMillis();

            AudioResult result = AudioResult.of(speechId, new byte[]{1}, OutputFormat.WAV);

            long after = System.currentTimeMillis();
            assertThat(result.timestamp()).isBetween(before, after);
        }
    }

    @Nested
    @DisplayName("sizeInBytes method")
    class SizeInBytesMethodTests {

        @Test
        @DisplayName("should return correct size for non-null data")
        void shouldReturnCorrectSizeForNonNullData() {
            byte[] audioData = new byte[]{1, 2, 3, 4, 5};
            AudioResult result = AudioResult.of(SpeechId.generate(), audioData, OutputFormat.MP3);

            assertThat(result.sizeInBytes()).isEqualTo(5);
        }

        @Test
        @DisplayName("should return 0 for null data")
        void shouldReturn0ForNullData() {
            AudioResult result = AudioResult.of(SpeechId.generate(), null, OutputFormat.MP3);

            assertThat(result.sizeInBytes()).isZero();
        }
    }

    @Nested
    @DisplayName("mediaType method")
    class MediaTypeMethodTests {

        @Test
        @DisplayName("should delegate to format for MP3")
        void shouldDelegateToFormatForMp3() {
            AudioResult result = AudioResult.of(SpeechId.generate(), new byte[]{1}, OutputFormat.MP3);

            assertThat(result.mediaType()).isEqualTo("audio/mpeg");
        }

        @Test
        @DisplayName("should delegate to format for WAV")
        void shouldDelegateToFormatForWav() {
            AudioResult result = AudioResult.of(SpeechId.generate(), new byte[]{1}, OutputFormat.WAV);

            assertThat(result.mediaType()).isEqualTo("audio/wav");
        }
    }

    @Nested
    @DisplayName("extension method")
    class ExtensionMethodTests {

        @Test
        @DisplayName("should delegate to format for MP3")
        void shouldDelegateToFormatForMp3() {
            AudioResult result = AudioResult.of(SpeechId.generate(), new byte[]{1}, OutputFormat.MP3);

            assertThat(result.extension()).isEqualTo("mp3");
        }

        @Test
        @DisplayName("should delegate to format for WAV")
        void shouldDelegateToFormatForWav() {
            AudioResult result = AudioResult.of(SpeechId.generate(), new byte[]{1}, OutputFormat.WAV);

            assertThat(result.extension()).isEqualTo("wav");
        }
    }

    @Nested
    @DisplayName("record accessors")
    class RecordAccessorTests {

        @Test
        @DisplayName("should provide all accessors")
        void shouldProvideAllAccessors() {
            SpeechId speechId = SpeechId.generate();
            byte[] data = new byte[]{1, 2};
            AudioResult result = AudioResult.of(speechId, data, OutputFormat.OGG);

            assertThat(result.speechId()).isEqualTo(speechId);
            assertThat(result.audioData()).isEqualTo(data);
            assertThat(result.format()).isEqualTo(OutputFormat.OGG);
            assertThat(result.timestamp()).isNotNull();
        }
    }
}
