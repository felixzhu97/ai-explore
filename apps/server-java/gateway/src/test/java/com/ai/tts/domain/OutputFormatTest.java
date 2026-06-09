package com.ai.tts.domain;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("OutputFormat Tests")
class OutputFormatTest {

    @Nested
    @DisplayName("enum values")
    class EnumValuesTests {

        @Test
        @DisplayName("should contain MP3 format")
        void shouldContainMp3Format() {
            assertThat(OutputFormat.MP3).isNotNull();
            assertThat(OutputFormat.MP3.name()).isEqualTo("MP3");
        }

        @Test
        @DisplayName("should contain WAV format")
        void shouldContainWavFormat() {
            assertThat(OutputFormat.WAV).isNotNull();
            assertThat(OutputFormat.WAV.name()).isEqualTo("WAV");
        }

        @Test
        @DisplayName("should contain OGG format")
        void shouldContainOggFormat() {
            assertThat(OutputFormat.OGG).isNotNull();
            assertThat(OutputFormat.OGG.name()).isEqualTo("OGG");
        }

        @Test
        @DisplayName("should contain FLAC format")
        void shouldContainFlacFormat() {
            assertThat(OutputFormat.FLAC).isNotNull();
            assertThat(OutputFormat.FLAC.name()).isEqualTo("FLAC");
        }

        @Test
        @DisplayName("should have all expected formats")
        void shouldHaveAllExpectedFormats() {
            assertThat(OutputFormat.values()).hasSize(4);
        }
    }

    @Nested
    @DisplayName("mediaType accessor")
    class MediaTypeAccessorTests {

        @Test
        @DisplayName("MP3 should return correct media type")
        void mp3ShouldReturnCorrectMediaType() {
            assertThat(OutputFormat.MP3.mediaType()).isEqualTo("audio/mpeg");
        }

        @Test
        @DisplayName("WAV should return correct media type")
        void wavShouldReturnCorrectMediaType() {
            assertThat(OutputFormat.WAV.mediaType()).isEqualTo("audio/wav");
        }

        @Test
        @DisplayName("OGG should return correct media type")
        void oggShouldReturnCorrectMediaType() {
            assertThat(OutputFormat.OGG.mediaType()).isEqualTo("audio/ogg");
        }

        @Test
        @DisplayName("FLAC should return correct media type")
        void flacShouldReturnCorrectMediaType() {
            assertThat(OutputFormat.FLAC.mediaType()).isEqualTo("audio/flac");
        }
    }

    @Nested
    @DisplayName("extension accessor")
    class ExtensionAccessorTests {

        @Test
        @DisplayName("MP3 should return correct extension")
        void mp3ShouldReturnCorrectExtension() {
            assertThat(OutputFormat.MP3.extension()).isEqualTo("mp3");
        }

        @Test
        @DisplayName("WAV should return correct extension")
        void wavShouldReturnCorrectExtension() {
            assertThat(OutputFormat.WAV.extension()).isEqualTo("wav");
        }

        @Test
        @DisplayName("OGG should return correct extension")
        void oggShouldReturnCorrectExtension() {
            assertThat(OutputFormat.OGG.extension()).isEqualTo("ogg");
        }

        @Test
        @DisplayName("FLAC should return correct extension")
        void flacShouldReturnCorrectExtension() {
            assertThat(OutputFormat.FLAC.extension()).isEqualTo("flac");
        }
    }

    @Nested
    @DisplayName("valueOf method")
    class ValueOfMethodTests {

        @ParameterizedTest
        @EnumSource(OutputFormat.class)
        @DisplayName("should find format by name")
        void shouldFindFormatByName(OutputFormat format) {
            OutputFormat found = OutputFormat.valueOf(format.name());

            assertThat(found).isEqualTo(format);
        }

        @Test
        @DisplayName("should throw exception for invalid name")
        void shouldThrowExceptionForInvalidName() {
            assertThatThrownBy(() -> OutputFormat.valueOf("INVALID"))
                    .isInstanceOf(IllegalArgumentException.class);
        }
    }
}
