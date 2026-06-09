package com.ai.vision.domain;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("ImageId Tests")
class ImageIdTest {

    @Nested
    @DisplayName("generate factory method")
    class GenerateFactoryMethodTests {

        @Test
        @DisplayName("should generate unique UUID string")
        void shouldGenerateUniqueUuidString() {
            ImageId id1 = ImageId.generate();
            ImageId id2 = ImageId.generate();

            assertThat(id1.value()).isNotEqualTo(id2.value());
        }

        @Test
        @DisplayName("should generate valid UUID format")
        void shouldGenerateValidUuidFormat() {
            ImageId id = ImageId.generate();

            assertThat(id.value()).matches(
                    "[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}"
            );
        }

        @Test
        @DisplayName("should generate non-null value")
        void shouldGenerateNonNullValue() {
            ImageId id = ImageId.generate();

            assertThat(id.value()).isNotNull();
            assertThat(id.value()).isNotBlank();
        }
    }

    @Nested
    @DisplayName("of factory method")
    class OfFactoryMethodTests {

        @Test
        @DisplayName("should create ImageId from valid string")
        void shouldCreateImageIdFromValidString() {
            String value = "image-123";
            ImageId id = ImageId.of(value);

            assertThat(id.value()).isEqualTo(value);
        }

        @ParameterizedTest
        @ValueSource(strings = {"valid-id", "another-id-123", "ID-456"})
        @DisplayName("should create ImageId from various valid strings")
        void shouldCreateImageIdFromVariousValidStrings(String value) {
            ImageId id = ImageId.of(value);

            assertThat(id.value()).isEqualTo(value);
        }

        @ParameterizedTest
        @NullAndEmptySource
        @ValueSource(strings = {"   ", "\t", "\n"})
        @DisplayName("should throw exception for null or blank input")
        void shouldThrowExceptionForNullOrBlankInput(String value) {
            assertThatThrownBy(() -> ImageId.of(value))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("cannot be null or blank");
        }
    }

    @Nested
    @DisplayName("value accessor")
    class ValueAccessorTests {

        @Test
        @DisplayName("should return the stored value")
        void shouldReturnTheStoredValue() {
            String expectedValue = "test-image-id";
            ImageId id = ImageId.of(expectedValue);

            assertThat(id.value()).isEqualTo(expectedValue);
        }
    }

    @Nested
    @DisplayName("equals and hashCode")
    class EqualsAndHashCodeTests {

        @Test
        @DisplayName("should be equal when values match")
        void shouldBeEqualWhenValuesMatch() {
            ImageId id1 = ImageId.of("same-value");
            ImageId id2 = ImageId.of("same-value");

            assertThat(id1).isEqualTo(id2);
            assertThat(id1.hashCode()).isEqualTo(id2.hashCode());
        }

        @Test
        @DisplayName("should not be equal when values differ")
        void shouldNotBeEqualWhenValuesDiffer() {
            ImageId id1 = ImageId.of("value-one");
            ImageId id2 = ImageId.of("value-two");

            assertThat(id1).isNotEqualTo(id2);
        }

        @Test
        @DisplayName("should not be equal to different type")
        void shouldNotBeEqualToDifferentType() {
            ImageId id = ImageId.of("test");

            assertThat(id).isNotEqualTo("test");
        }
    }

    @Nested
    @DisplayName("toString")
    class ToStringTests {

        @Test
        @DisplayName("should return value as string")
        void shouldReturnValueAsString() {
            String value = "to-string-test";
            ImageId id = ImageId.of(value);

            assertThat(id.toString()).isEqualTo(value);
        }
    }
}
