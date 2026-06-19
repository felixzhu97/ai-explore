package com.ai.adapter.in.controller;

import com.ai.adapter.in.dto.ErrorResponse;
import com.ai.domain.exception.DocumentNotFoundException;
import com.ai.domain.exception.RagServiceException;
import com.ai.domain.exception.AiServiceException;
import com.ai.domain.model.ChatSessionNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.FieldError;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * GlobalExceptionHandler Tests
 */
@DisplayName("GlobalExceptionHandler Tests")
class GlobalExceptionHandlerTest {

    private GlobalExceptionHandler handler;

    @BeforeEach
    void setUp() {
        handler = new GlobalExceptionHandler();
    }

    @Nested
    @DisplayName("shouldReturn404_WhenSessionNotFound")
    class ChatSessionNotFoundTests {

        @Test
        @DisplayName("should return 404 when ChatSessionNotFoundException is thrown")
        void shouldReturn404WhenChatSessionNotFoundExceptionThrown() {
            String sessionId = "session-123";
            ChatSessionNotFoundException exception = new ChatSessionNotFoundException(sessionId);

            ResponseEntity<ErrorResponse> response = handler.handleSessionNotFound(exception);

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
            assertThat(response.getBody()).isNotNull();
            assertThat(response.getBody().errorCode()).isEqualTo("SESSION_NOT_FOUND");
        }

        @Test
        @DisplayName("should include session ID in error message")
        void shouldIncludeSessionIdInErrorMessage() {
            String sessionId = "abc-456";
            ChatSessionNotFoundException exception = new ChatSessionNotFoundException(sessionId);

            ResponseEntity<ErrorResponse> response = handler.handleSessionNotFound(exception);

            assertThat(response.getBody().message()).contains(sessionId);
        }
    }

    @Nested
    @DisplayName("shouldReturn404_WhenDocumentNotFound")
    class DocumentNotFoundTests {

        @Test
        @DisplayName("should return 404 when DocumentNotFoundException is thrown")
        void shouldReturn404WhenDocumentNotFoundExceptionThrown() {
            UUID docId = UUID.randomUUID();
            DocumentNotFoundException exception = new DocumentNotFoundException(docId);

            ResponseEntity<ErrorResponse> response = handler.handleDocumentNotFound(exception);

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
            assertThat(response.getBody()).isNotNull();
            assertThat(response.getBody().errorCode()).isEqualTo("DOCUMENT_NOT_FOUND");
        }
    }

    @Nested
    @DisplayName("shouldReturn503_WhenAiServiceError")
    class AiServiceExceptionTests {

        @Test
        @DisplayName("should return 503 when AiServiceException is thrown")
        void shouldReturn503WhenAiServiceExceptionThrown() {
            AiServiceException exception = new AiServiceException("AI service unavailable");

            ResponseEntity<ErrorResponse> response = handler.handleAiServiceError(exception);

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.SERVICE_UNAVAILABLE);
            assertThat(response.getBody()).isNotNull();
            assertThat(response.getBody().errorCode()).isEqualTo("AI_SERVICE_ERROR");
        }

        @Test
        @DisplayName("should handle AiServiceException with cause")
        void shouldHandleAiServiceExceptionWithCause() {
            Throwable cause = new RuntimeException("Connection timeout");
            AiServiceException exception = new AiServiceException("AI service failed", cause);

            ResponseEntity<ErrorResponse> response = handler.handleAiServiceError(exception);

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.SERVICE_UNAVAILABLE);
            assertThat(response.getBody().errorCode()).isEqualTo("AI_SERVICE_ERROR");
        }
    }

    @Nested
    @DisplayName("shouldReturn500_WhenRagServiceError")
    class RagServiceExceptionTests {

        @Test
        @DisplayName("should return 500 when RagServiceException is thrown")
        void shouldReturn500WhenRagServiceExceptionThrown() {
            RagServiceException exception = new RagServiceException("RAG processing failed");

            ResponseEntity<ErrorResponse> response = handler.handleRagServiceError(exception);

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
            assertThat(response.getBody()).isNotNull();
            assertThat(response.getBody().errorCode()).isEqualTo("RAG_SERVICE_ERROR");
        }
    }

    @Nested
    @DisplayName("shouldReturn400_WhenValidationError")
    class MethodArgumentNotValidExceptionTests {

        @Test
        @DisplayName("should return 400 when MethodArgumentNotValidException is thrown")
        void shouldReturn400WhenMethodArgumentNotValidExceptionThrown() {
            MethodArgumentNotValidException exception = createValidationException("message", "Message is required");

            ResponseEntity<ErrorResponse> response = handler.handleValidationError(exception);

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
            assertThat(response.getBody()).isNotNull();
            assertThat(response.getBody().errorCode()).isEqualTo("VALIDATION_ERROR");
        }

        @Test
        @DisplayName("should include field errors in message")
        void shouldIncludeFieldErrorsInMessage() {
            MethodArgumentNotValidException exception = createValidationException("message", "must not be blank");

            ResponseEntity<ErrorResponse> response = handler.handleValidationError(exception);

            assertThat(response.getBody().message()).contains("message:");
        }
    }

    @Nested
    @DisplayName("shouldReturn400_WhenIllegalArgument")
    class IllegalArgumentExceptionTests {

        @Test
        @DisplayName("should return 400 when IllegalArgumentException is thrown")
        void shouldReturn400WhenIllegalArgumentExceptionThrown() {
            IllegalArgumentException exception = new IllegalArgumentException("Invalid parameter");

            ResponseEntity<ErrorResponse> response = handler.handleIllegalArgument(exception);

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
            assertThat(response.getBody()).isNotNull();
            assertThat(response.getBody().errorCode()).isEqualTo("BAD_REQUEST");
        }
    }

    @Nested
    @DisplayName("shouldReturn413_WhenFileTooLarge")
    class MaxUploadSizeExceededExceptionTests {

        @Test
        @DisplayName("should return 413 when MaxUploadSizeExceededException is thrown")
        void shouldReturn413WhenMaxUploadSizeExceededExceptionThrown() {
            org.springframework.web.multipart.MaxUploadSizeExceededException exception = 
                new org.springframework.web.multipart.MaxUploadSizeExceededException(52428800L);

            ResponseEntity<ErrorResponse> response = handler.handleMaxUploadSizeExceeded(exception);

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.PAYLOAD_TOO_LARGE);
            assertThat(response.getBody()).isNotNull();
            assertThat(response.getBody().errorCode()).isEqualTo("FILE_TOO_LARGE");
        }
    }

    @Nested
    @DisplayName("shouldReturn500_WhenGenericException")
    class GenericExceptionTests {

        @Test
        @DisplayName("should return 500 when generic Exception is thrown")
        void shouldReturn500WhenGenericExceptionThrown() {
            Exception exception = new Exception("Something went wrong");

            ResponseEntity<ErrorResponse> response = handler.handleGenericException(exception);

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
            assertThat(response.getBody()).isNotNull();
            assertThat(response.getBody().errorCode()).isEqualTo("INTERNAL_ERROR");
        }

        @Test
        @DisplayName("should hide internal error details from client")
        void shouldHideInternalErrorDetailsFromClient() {
            Exception exception = new RuntimeException("Database connection failed");

            ResponseEntity<ErrorResponse> response = handler.handleGenericException(exception);

            assertThat(response.getBody().message()).isEqualTo("An unexpected error occurred");
        }
    }

    private MethodArgumentNotValidException createValidationException(String field, String message) {
        BeanPropertyBindingResult bindingResult = new BeanPropertyBindingResult(new TestRequest(), "testRequest");
        bindingResult.addError(new FieldError("testRequest", field, message));
        return new MethodArgumentNotValidException(null, bindingResult);
    }

    static class TestRequest {
        private String message;
        public String getMessage() { return message; }
        public void setMessage(String message) { this.message = message; }
    }
}
