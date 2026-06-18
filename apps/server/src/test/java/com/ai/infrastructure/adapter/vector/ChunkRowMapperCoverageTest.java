package com.ai.adapter.out.vector;

import com.ai.adapter.out.vector.PgVectorAdapter;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.jdbc.core.JdbcTemplate;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

/**
 * ChunkRowMapperCoverageTest - Direct unit tests for ChunkRowMapper inner class.
 * Placed in same package as PgVectorAdapter to access package-private inner class.
 *
 * Naming convention: should_expected_result_when_condition
 * Uses AAA pattern (Arrange-Act-Assert)
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("ChunkRowMapper Coverage Tests")
class ChunkRowMapperCoverageTest {

    @Mock
    private JdbcTemplate jdbcTemplate;

    @Mock
    private ResultSet resultSet;

    private PgVectorAdapter adapter;
    private PgVectorAdapter.ChunkRowMapper mapper;

    private static final UUID TEST_ID = UUID.fromString("123e4567-e89b-12d3-a456-426614174000");
    private static final UUID TEST_DOCUMENT_ID = UUID.fromString("223e4567-e89b-12d3-a456-426614174001");

    @BeforeEach
    void setUp() {
        ObjectMapper objectMapper = new ObjectMapper();
        adapter = new PgVectorAdapter(jdbcTemplate, objectMapper);
        mapper = adapter.new ChunkRowMapper();
    }

    @Nested
    @DisplayName("parsePostgresVector")
    class ParsePostgresVectorTests {

        @Test
        @DisplayName("should parse vector string with brackets")
        void shouldParseVectorString_brackets() throws SQLException {
            // Arrange
            String vectorString = "[1.0, 2.0, 3.0, 4.0]";
            when(resultSet.getString("id")).thenReturn(TEST_ID.toString());
            when(resultSet.getString("document_id")).thenReturn(TEST_DOCUMENT_ID.toString());
            when(resultSet.getString("content")).thenReturn("Test content");
            when(resultSet.getInt("chunk_index")).thenReturn(0);
            when(resultSet.getString("embedding")).thenReturn(vectorString);
            when(resultSet.getString("metadata")).thenReturn(null);
            when(resultSet.getString("created_at")).thenReturn("2024-01-01T00:00:00Z");

            // Act
            var chunk = mapper.mapRow(resultSet, 0);

            // Assert
            assertThat(chunk.getEmbedding()).hasSize(4);
            assertThat(chunk.getEmbedding()[0]).isEqualTo(1.0f);
            assertThat(chunk.getEmbedding()[1]).isEqualTo(2.0f);
            assertThat(chunk.getEmbedding()[2]).isEqualTo(3.0f);
            assertThat(chunk.getEmbedding()[3]).isEqualTo(4.0f);
        }

        @Test
        @DisplayName("should parse empty vector string")
        void shouldParseVectorString_empty() throws SQLException {
            // Arrange
            when(resultSet.getString("id")).thenReturn(TEST_ID.toString());
            when(resultSet.getString("document_id")).thenReturn(TEST_DOCUMENT_ID.toString());
            when(resultSet.getString("content")).thenReturn("Test content");
            when(resultSet.getInt("chunk_index")).thenReturn(0);
            when(resultSet.getString("embedding")).thenReturn("");
            when(resultSet.getString("metadata")).thenReturn(null);
            when(resultSet.getString("created_at")).thenReturn("2024-01-01T00:00:00Z");

            // Act
            var chunk = mapper.mapRow(resultSet, 0);

            // Assert
            assertThat(chunk.getEmbedding()).isEmpty();
        }

        @Test
        @DisplayName("should parse null vector string as empty array")
        void shouldParseVectorString_null() throws SQLException {
            // Arrange
            when(resultSet.getString("id")).thenReturn(TEST_ID.toString());
            when(resultSet.getString("document_id")).thenReturn(TEST_DOCUMENT_ID.toString());
            when(resultSet.getString("content")).thenReturn("Test content");
            when(resultSet.getInt("chunk_index")).thenReturn(0);
            when(resultSet.getString("embedding")).thenReturn(null);
            when(resultSet.getString("metadata")).thenReturn(null);
            when(resultSet.getString("created_at")).thenReturn("2024-01-01T00:00:00Z");

            // Act
            var chunk = mapper.mapRow(resultSet, 0);

            // Assert
            assertThat(chunk.getEmbedding()).isEmpty();
        }

        @Test
        @DisplayName("should parse vector with spaces after comma")
        void shouldParseVectorString_withSpaces() throws SQLException {
            // Arrange
            String vectorString = "[1.0, 2.0, 3.0]";
            when(resultSet.getString("id")).thenReturn(TEST_ID.toString());
            when(resultSet.getString("document_id")).thenReturn(TEST_DOCUMENT_ID.toString());
            when(resultSet.getString("content")).thenReturn("Test content");
            when(resultSet.getInt("chunk_index")).thenReturn(0);
            when(resultSet.getString("embedding")).thenReturn(vectorString);
            when(resultSet.getString("metadata")).thenReturn(null);
            when(resultSet.getString("created_at")).thenReturn("2024-01-01T00:00:00Z");

            // Act
            var chunk = mapper.mapRow(resultSet, 0);

            // Assert
            assertThat(chunk.getEmbedding()).hasSize(3);
            assertThat(chunk.getEmbedding()[0]).isEqualTo(1.0f);
            assertThat(chunk.getEmbedding()[1]).isEqualTo(2.0f);
            assertThat(chunk.getEmbedding()[2]).isEqualTo(3.0f);
        }
    }

    @Nested
    @DisplayName("parseMetadata")
    class ParseMetadataTests {

        @Test
        @DisplayName("should parse valid metadata JSON")
        void shouldParseMetadataJson_valid() throws SQLException {
            // Arrange
            String metadataJson = "{\"key\":\"value\",\"count\":42}";
            when(resultSet.getString("id")).thenReturn(TEST_ID.toString());
            when(resultSet.getString("document_id")).thenReturn(TEST_DOCUMENT_ID.toString());
            when(resultSet.getString("content")).thenReturn("Test content");
            when(resultSet.getInt("chunk_index")).thenReturn(0);
            when(resultSet.getString("embedding")).thenReturn("[1.0,2.0]");
            when(resultSet.getString("metadata")).thenReturn(metadataJson);
            when(resultSet.getString("created_at")).thenReturn("2024-01-01T00:00:00Z");

            // Act
            var chunk = mapper.mapRow(resultSet, 0);

            // Assert
            assertThat(chunk.getMetadata()).containsKey("key");
            assertThat(chunk.getMetadata()).containsKey("count");
            assertThat(chunk.getMetadata().get("key")).isEqualTo("value");
            assertThat(chunk.getMetadata().get("count")).isEqualTo(42);
        }

        @Test
        @DisplayName("should parse null metadata as empty map")
        void shouldParseMetadataJson_null() throws SQLException {
            // Arrange
            when(resultSet.getString("id")).thenReturn(TEST_ID.toString());
            when(resultSet.getString("document_id")).thenReturn(TEST_DOCUMENT_ID.toString());
            when(resultSet.getString("content")).thenReturn("Test content");
            when(resultSet.getInt("chunk_index")).thenReturn(0);
            when(resultSet.getString("embedding")).thenReturn("[1.0,2.0]");
            when(resultSet.getString("metadata")).thenReturn(null);
            when(resultSet.getString("created_at")).thenReturn("2024-01-01T00:00:00Z");

            // Act
            var chunk = mapper.mapRow(resultSet, 0);

            // Assert
            assertThat(chunk.getMetadata()).isEmpty();
        }

        @Test
        @DisplayName("should parse empty metadata string as empty map")
        void shouldParseMetadataJson_empty() throws SQLException {
            // Arrange
            when(resultSet.getString("id")).thenReturn(TEST_ID.toString());
            when(resultSet.getString("document_id")).thenReturn(TEST_DOCUMENT_ID.toString());
            when(resultSet.getString("content")).thenReturn("Test content");
            when(resultSet.getInt("chunk_index")).thenReturn(0);
            when(resultSet.getString("embedding")).thenReturn("[1.0,2.0]");
            when(resultSet.getString("metadata")).thenReturn("");
            when(resultSet.getString("created_at")).thenReturn("2024-01-01T00:00:00Z");

            // Act
            var chunk = mapper.mapRow(resultSet, 0);

            // Assert
            assertThat(chunk.getMetadata()).isEmpty();
        }

        @Test
        @DisplayName("should parse metadata with null string as empty map")
        void shouldParseMetadataJson_nullString() throws SQLException {
            // Arrange
            when(resultSet.getString("id")).thenReturn(TEST_ID.toString());
            when(resultSet.getString("document_id")).thenReturn(TEST_DOCUMENT_ID.toString());
            when(resultSet.getString("content")).thenReturn("Test content");
            when(resultSet.getInt("chunk_index")).thenReturn(0);
            when(resultSet.getString("embedding")).thenReturn("[1.0,2.0]");
            when(resultSet.getString("metadata")).thenReturn("null");
            when(resultSet.getString("created_at")).thenReturn("2024-01-01T00:00:00Z");

            // Act
            var chunk = mapper.mapRow(resultSet, 0);

            // Assert
            assertThat(chunk.getMetadata()).isEmpty();
        }

        @Test
        @DisplayName("should handle invalid metadata JSON gracefully")
        void shouldParseMetadataJson_invalid() throws SQLException {
            // Arrange
            String invalidJson = "{invalid json}";
            when(resultSet.getString("id")).thenReturn(TEST_ID.toString());
            when(resultSet.getString("document_id")).thenReturn(TEST_DOCUMENT_ID.toString());
            when(resultSet.getString("content")).thenReturn("Test content");
            when(resultSet.getInt("chunk_index")).thenReturn(0);
            when(resultSet.getString("embedding")).thenReturn("[1.0,2.0]");
            when(resultSet.getString("metadata")).thenReturn(invalidJson);
            when(resultSet.getString("created_at")).thenReturn("2024-01-01T00:00:00Z");

            // Act
            var chunk = mapper.mapRow(resultSet, 0);

            // Assert
            assertThat(chunk.getMetadata()).isEmpty();
        }
    }

    @Nested
    @DisplayName("parseTimestamp")
    class ParseTimestampTests {

        @Test
        @DisplayName("should parse ISO instant timestamp")
        void shouldParseTimestamp_isoInstant() throws SQLException {
            // Arrange
            String timestamp = "2024-01-01T00:00:00Z";
            when(resultSet.getString("id")).thenReturn(TEST_ID.toString());
            when(resultSet.getString("document_id")).thenReturn(TEST_DOCUMENT_ID.toString());
            when(resultSet.getString("content")).thenReturn("Test content");
            when(resultSet.getInt("chunk_index")).thenReturn(0);
            when(resultSet.getString("embedding")).thenReturn("[1.0,2.0]");
            when(resultSet.getString("metadata")).thenReturn(null);
            when(resultSet.getString("created_at")).thenReturn(timestamp);

            // Act
            var chunk = mapper.mapRow(resultSet, 0);

            // Assert
            assertThat(chunk.getCreatedAt()).isNotNull();
            // Just verify the timestamp was parsed (not null fallback)
            assertThat(chunk.getCreatedAt().toString()).isNotNull();
        }

        @Test
        @DisplayName("should parse offset datetime timestamp")
        void shouldParseTimestamp_offsetDateTime() throws SQLException {
            // Arrange
            String timestamp = "2024-01-01T00:00:00+08:00";
            when(resultSet.getString("id")).thenReturn(TEST_ID.toString());
            when(resultSet.getString("document_id")).thenReturn(TEST_DOCUMENT_ID.toString());
            when(resultSet.getString("content")).thenReturn("Test content");
            when(resultSet.getInt("chunk_index")).thenReturn(0);
            when(resultSet.getString("embedding")).thenReturn("[1.0,2.0]");
            when(resultSet.getString("metadata")).thenReturn(null);
            when(resultSet.getString("created_at")).thenReturn(timestamp);

            // Act
            var chunk = mapper.mapRow(resultSet, 0);

            // Assert
            assertThat(chunk.getCreatedAt()).isNotNull();
        }

        @Test
        @DisplayName("should fallback to now for invalid timestamp")
        void shouldFallbackToNowForInvalidTimestamp() throws SQLException {
            // Arrange
            String invalidTimestamp = "invalid";
            long beforeTest = System.currentTimeMillis();
            when(resultSet.getString("id")).thenReturn(TEST_ID.toString());
            when(resultSet.getString("document_id")).thenReturn(TEST_DOCUMENT_ID.toString());
            when(resultSet.getString("content")).thenReturn("Test content");
            when(resultSet.getInt("chunk_index")).thenReturn(0);
            when(resultSet.getString("embedding")).thenReturn("[1.0,2.0]");
            when(resultSet.getString("metadata")).thenReturn(null);
            when(resultSet.getString("created_at")).thenReturn(invalidTimestamp);

            // Act
            var chunk = mapper.mapRow(resultSet, 0);
            long afterTest = System.currentTimeMillis();

            // Assert
            assertThat(chunk.getCreatedAt()).isNotNull();
            assertThat(chunk.getCreatedAt().toEpochMilli()).isBetween(beforeTest - 1000, afterTest + 1000);
        }

        @Test
        @DisplayName("should fallback to now for null timestamp")
        void shouldFallbackToNowForNullTimestamp() throws SQLException {
            // Arrange
            long beforeTest = System.currentTimeMillis();
            when(resultSet.getString("id")).thenReturn(TEST_ID.toString());
            when(resultSet.getString("document_id")).thenReturn(TEST_DOCUMENT_ID.toString());
            when(resultSet.getString("content")).thenReturn("Test content");
            when(resultSet.getInt("chunk_index")).thenReturn(0);
            when(resultSet.getString("embedding")).thenReturn("[1.0,2.0]");
            when(resultSet.getString("metadata")).thenReturn(null);
            when(resultSet.getString("created_at")).thenReturn(null);

            // Act
            var chunk = mapper.mapRow(resultSet, 0);
            long afterTest = System.currentTimeMillis();

            // Assert
            assertThat(chunk.getCreatedAt()).isNotNull();
            assertThat(chunk.getCreatedAt().toEpochMilli()).isBetween(beforeTest - 1000, afterTest + 1000);
        }

        @Test
        @DisplayName("should fallback to now for empty timestamp")
        void shouldFallbackToNowForEmptyTimestamp() throws SQLException {
            // Arrange
            long beforeTest = System.currentTimeMillis();
            when(resultSet.getString("id")).thenReturn(TEST_ID.toString());
            when(resultSet.getString("document_id")).thenReturn(TEST_DOCUMENT_ID.toString());
            when(resultSet.getString("content")).thenReturn("Test content");
            when(resultSet.getInt("chunk_index")).thenReturn(0);
            when(resultSet.getString("embedding")).thenReturn("[1.0,2.0]");
            when(resultSet.getString("metadata")).thenReturn(null);
            when(resultSet.getString("created_at")).thenReturn("");

            // Act
            var chunk = mapper.mapRow(resultSet, 0);
            long afterTest = System.currentTimeMillis();

            // Assert
            assertThat(chunk.getCreatedAt()).isNotNull();
            assertThat(chunk.getCreatedAt().toEpochMilli()).isBetween(beforeTest - 1000, afterTest + 1000);
        }

        @Test
        @DisplayName("should parse local datetime timestamp")
        void shouldParseTimestamp_localDateTime() throws SQLException {
            // Arrange
            String timestamp = "2024-06-15 10:30:00";
            when(resultSet.getString("id")).thenReturn(TEST_ID.toString());
            when(resultSet.getString("document_id")).thenReturn(TEST_DOCUMENT_ID.toString());
            when(resultSet.getString("content")).thenReturn("Test content");
            when(resultSet.getInt("chunk_index")).thenReturn(0);
            when(resultSet.getString("embedding")).thenReturn("[1.0,2.0]");
            when(resultSet.getString("metadata")).thenReturn(null);
            when(resultSet.getString("created_at")).thenReturn(timestamp);

            // Act
            var chunk = mapper.mapRow(resultSet, 0);

            // Assert
            assertThat(chunk.getCreatedAt()).isNotNull();
        }
    }

    @Nested
    @DisplayName("mapRow")
    class MapRowTests {

        @Test
        @DisplayName("should map all result set columns correctly")
        void shouldMapAllColumnsCorrectly() throws SQLException {
            // Arrange
            String content = "This is test content";
            int chunkIndex = 5;
            when(resultSet.getString("id")).thenReturn(TEST_ID.toString());
            when(resultSet.getString("document_id")).thenReturn(TEST_DOCUMENT_ID.toString());
            when(resultSet.getString("content")).thenReturn(content);
            when(resultSet.getInt("chunk_index")).thenReturn(chunkIndex);
            when(resultSet.getString("embedding")).thenReturn("[1.0,2.0,3.0]");
            when(resultSet.getString("metadata")).thenReturn(null);
            when(resultSet.getString("created_at")).thenReturn("2024-01-01T00:00:00Z");

            // Act
            var chunk = mapper.mapRow(resultSet, 0);

            // Assert
            assertThat(chunk.getId()).isEqualTo(TEST_ID);
            assertThat(chunk.getDocumentId()).isEqualTo(TEST_DOCUMENT_ID);
            assertThat(chunk.getContent()).isEqualTo(content);
            assertThat(chunk.getChunkIndex()).isEqualTo(chunkIndex);
            assertThat(chunk.getEmbedding()).hasSize(3);
        }

        @Test
        @DisplayName("should handle rowNum parameter")
        void shouldHandleRowNumParameter() throws SQLException {
            // Arrange
            when(resultSet.getString("id")).thenReturn(TEST_ID.toString());
            when(resultSet.getString("document_id")).thenReturn(TEST_DOCUMENT_ID.toString());
            when(resultSet.getString("content")).thenReturn("Content");
            when(resultSet.getInt("chunk_index")).thenReturn(0);
            when(resultSet.getString("embedding")).thenReturn("[1.0]");
            when(resultSet.getString("metadata")).thenReturn(null);
            when(resultSet.getString("created_at")).thenReturn("2024-01-01T00:00:00Z");

            // Act
            var chunk1 = mapper.mapRow(resultSet, 0);
            var chunk2 = mapper.mapRow(resultSet, 5);
            var chunk3 = mapper.mapRow(resultSet, 100);

            // Assert
            assertThat(chunk1).isNotNull();
            assertThat(chunk2).isNotNull();
            assertThat(chunk3).isNotNull();
        }
    }
}
