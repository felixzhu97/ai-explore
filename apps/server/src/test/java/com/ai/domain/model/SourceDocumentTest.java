package com.ai.domain.model;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * SourceDocument Record Tests
 * 
 * Tests for SourceDocument immutable record following TDD principles:
 * - Naming convention: should_expected_result_when_condition
 * - Uses AAA pattern (Arrange-Act-Assert)
 * - Tests record creation and equality
 */
@DisplayName("SourceDocument")
class SourceDocumentTest {

    @Nested
    @DisplayName("Creation")
    class Creation {

        @Test
        @DisplayName("should create with all fields")
        void shouldCreateWithAllFields() {
            // Arrange
            int index = 1;
            String text = "This is the document text.";
            double score = 0.95;
            String documentTitle = "Test Document";
            Map<String, Object> metadata = new HashMap<>();
            metadata.put("source", "test.pdf");
            metadata.put("page", 1);

            // Act
            SourceDocument sourceDocument = new SourceDocument(index, text, score, documentTitle, metadata);

            // Assert
            assertThat(sourceDocument.index()).isEqualTo(index);
            assertThat(sourceDocument.text()).isEqualTo(text);
            assertThat(sourceDocument.score()).isEqualTo(score);
            assertThat(sourceDocument.documentTitle()).isEqualTo(documentTitle);
            assertThat(sourceDocument.metadata()).isEqualTo(metadata);
        }

        @Test
        @DisplayName("should create with empty metadata")
        void shouldCreateWithEmptyMetadata() {
            // Act
            SourceDocument sourceDocument = new SourceDocument(1, "text", 0.5, "title", new HashMap<>());

            // Assert
            assertThat(sourceDocument.metadata()).isEmpty();
        }

        @Test
        @DisplayName("should create with null metadata")
        void shouldCreateWithNullMetadata() {
            // Act
            SourceDocument sourceDocument = new SourceDocument(1, "text", 0.5, "title", null);

            // Assert
            assertThat(sourceDocument.metadata()).isNull();
        }

        @Test
        @DisplayName("should create with zero score")
        void shouldCreateWithZeroScore() {
            // Act
            SourceDocument sourceDocument = new SourceDocument(1, "text", 0.0, "title", new HashMap<>());

            // Assert
            assertThat(sourceDocument.score()).isZero();
        }

        @Test
        @DisplayName("should create with negative score")
        void shouldCreateWithNegativeScore() {
            // Act
            SourceDocument sourceDocument = new SourceDocument(1, "text", -0.5, "title", new HashMap<>());

            // Assert
            assertThat(sourceDocument.score()).isNegative();
        }

        @Test
        @DisplayName("should create with empty text")
        void shouldCreateWithEmptyText() {
            // Act
            SourceDocument sourceDocument = new SourceDocument(1, "", 0.5, "title", new HashMap<>());

            // Assert
            assertThat(sourceDocument.text()).isEmpty();
        }

        @Test
        @DisplayName("should handle special characters in text")
        void shouldHandleSpecialCharactersInText() {
            // Arrange
            String specialText = "Hello! ¿Cómo estás? 你好世界! 🎉";

            // Act
            SourceDocument sourceDocument = new SourceDocument(1, specialText, 0.9, "title", new HashMap<>());

            // Assert
            assertThat(sourceDocument.text()).isEqualTo(specialText);
        }
    }

    @Nested
    @DisplayName("Equality")
    class Equality {

        @Test
        @DisplayName("should be equal when same values")
        void shouldBeEqualWhenSameValues() {
            // Arrange
            Map<String, Object> metadata1 = new HashMap<>();
            metadata1.put("key", "value");
            Map<String, Object> metadata2 = new HashMap<>();
            metadata2.put("key", "value");

            SourceDocument doc1 = new SourceDocument(1, "text", 0.5, "title", metadata1);
            SourceDocument doc2 = new SourceDocument(1, "text", 0.5, "title", metadata2);

            // Assert
            assertThat(doc1).isEqualTo(doc2);
        }

        @Test
        @DisplayName("should not be equal when different index")
        void shouldNotBeEqualWhenDifferentIndex() {
            // Arrange
            SourceDocument doc1 = new SourceDocument(1, "text", 0.5, "title", new HashMap<>());
            SourceDocument doc2 = new SourceDocument(2, "text", 0.5, "title", new HashMap<>());

            // Assert
            assertThat(doc1).isNotEqualTo(doc2);
        }

        @Test
        @DisplayName("should not be equal when different text")
        void shouldNotBeEqualWhenDifferentText() {
            // Arrange
            SourceDocument doc1 = new SourceDocument(1, "text1", 0.5, "title", new HashMap<>());
            SourceDocument doc2 = new SourceDocument(1, "text2", 0.5, "title", new HashMap<>());

            // Assert
            assertThat(doc1).isNotEqualTo(doc2);
        }

        @Test
        @DisplayName("should not be equal when different score")
        void shouldNotBeEqualWhenDifferentScore() {
            // Arrange
            SourceDocument doc1 = new SourceDocument(1, "text", 0.5, "title", new HashMap<>());
            SourceDocument doc2 = new SourceDocument(1, "text", 0.6, "title", new HashMap<>());

            // Assert
            assertThat(doc1).isNotEqualTo(doc2);
        }

        @Test
        @DisplayName("should not be equal when different metadata")
        void shouldNotBeEqualWhenDifferentMetadata() {
            // Arrange
            Map<String, Object> metadata1 = new HashMap<>();
            metadata1.put("key", "value1");
            Map<String, Object> metadata2 = new HashMap<>();
            metadata2.put("key", "value2");

            SourceDocument doc1 = new SourceDocument(1, "text", 0.5, "title", metadata1);
            SourceDocument doc2 = new SourceDocument(1, "text", 0.5, "title", metadata2);

            // Assert
            assertThat(doc1).isNotEqualTo(doc2);
        }

        @Test
        @DisplayName("should not be equal to null")
        void shouldNotBeEqualToNull() {
            // Arrange
            SourceDocument doc = new SourceDocument(1, "text", 0.5, "title", new HashMap<>());

            // Assert
            assertThat(doc).isNotEqualTo(null);
        }

        @Test
        @DisplayName("should not be equal to different type")
        void shouldNotBeEqualToDifferentType() {
            // Arrange
            SourceDocument doc = new SourceDocument(1, "text", 0.5, "title", new HashMap<>());

            // Assert
            assertThat(doc).isNotEqualTo("not a source document");
        }

        @Test
        @DisplayName("should be reflexive")
        void shouldBeReflexive() {
            // Arrange
            SourceDocument doc = new SourceDocument(1, "text", 0.5, "title", new HashMap<>());

            // Assert
            assertThat(doc).isEqualTo(doc);
        }
    }

    @Nested
    @DisplayName("HashCode")
    class HashCode {

        @Test
        @DisplayName("should have same hashCode for equal instances")
        void shouldHaveSameHashCodeForEqualInstances() {
            // Arrange
            Map<String, Object> metadata1 = new HashMap<>();
            metadata1.put("key", "value");
            Map<String, Object> metadata2 = new HashMap<>();
            metadata2.put("key", "value");

            SourceDocument doc1 = new SourceDocument(1, "text", 0.5, "title", metadata1);
            SourceDocument doc2 = new SourceDocument(1, "text", 0.5, "title", metadata2);

            // Assert
            assertThat(doc1.hashCode()).isEqualTo(doc2.hashCode());
        }

        @Test
        @DisplayName("should have different hashCode for different instances")
        void shouldHaveDifferentHashCodeForDifferentInstances() {
            // Arrange
            SourceDocument doc1 = new SourceDocument(1, "text1", 0.5, "title", new HashMap<>());
            SourceDocument doc2 = new SourceDocument(2, "text1", 0.5, "title", new HashMap<>());

            // Assert
            assertThat(doc1.hashCode()).isNotEqualTo(doc2.hashCode());
        }
    }

    @Nested
    @DisplayName("Record Accessors")
    class RecordAccessors {

        @Test
        @DisplayName("should support record accessor methods")
        void shouldSupportRecordAccessorMethods() {
            // Arrange
            Map<String, Object> metadata = new HashMap<>();
            metadata.put("source", "test");

            // Act
            SourceDocument doc = new SourceDocument(1, "test text", 0.8, "Test Document", metadata);

            // Assert
            assertThat(doc.index()).isEqualTo(1);
            assertThat(doc.text()).isNotNull();
            assertThat(doc.score()).isNotNull();
            assertThat(doc.documentTitle()).isEqualTo("Test Document");
            assertThat(doc.metadata()).isNotNull();
        }
    }
}
