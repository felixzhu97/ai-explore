package com.ai.vision.infrastructure.exception;

import com.ai.vision.domain.exception.VisionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import reactor.core.publisher.Mono;

import java.time.Instant;
import java.util.Map;

/**
 * Global exception handler for Vision Service.
 * 
 * Converts domain exceptions and infrastructure exceptions
 * into appropriate HTTP responses.
 */
@RestControllerAdvice
public class VisionExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(VisionExceptionHandler.class);

    @ExceptionHandler(VisionException.class)
    public Mono<ResponseEntity<Map<String, Object>>> handleVisionException(VisionException e) {
        log.error("Vision error [{}]: {}", e.errorCode(), e.getMessage());

        HttpStatus status = switch (e.errorCode()) {
            case MODEL_NOT_AVAILABLE -> HttpStatus.SERVICE_UNAVAILABLE;
            case INVALID_IMAGE_DATA -> HttpStatus.BAD_REQUEST;
            case STATE_TRANSITION_INVALID -> HttpStatus.CONFLICT;
            case TASK_NOT_SUPPORTED -> HttpStatus.NOT_IMPLEMENTED;
            case UNSUPPORTED_OPERATION -> HttpStatus.NOT_IMPLEMENTED;
            default -> HttpStatus.INTERNAL_SERVER_ERROR;
        };

        return Mono.just(ResponseEntity
            .status(status)
            .body(Map.of(
                "error", e.errorCode().name(),
                "message", e.getMessage(),
                "details", e.details() != null ? e.details() : "",
                "timestamp", Instant.now().toString()
            )));
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public Mono<ResponseEntity<Map<String, Object>>> handleIllegalArgument(IllegalArgumentException e) {
        log.warn("Bad request: {}", e.getMessage());
        return Mono.just(ResponseEntity
            .status(HttpStatus.BAD_REQUEST)
            .body(Map.of(
                "error", "Bad Request",
                "message", e.getMessage(),
                "timestamp", Instant.now().toString()
            )));
    }

    @ExceptionHandler(IllegalStateException.class)
    public Mono<ResponseEntity<Map<String, Object>>> handleIllegalState(IllegalStateException e) {
        log.warn("Service unavailable: {}", e.getMessage());
        return Mono.just(ResponseEntity
            .status(HttpStatus.SERVICE_UNAVAILABLE)
            .body(Map.of(
                "error", "Service Unavailable",
                "message", e.getMessage(),
                "timestamp", Instant.now().toString()
            )));
    }

    @ExceptionHandler(UnsupportedOperationException.class)
    public Mono<ResponseEntity<Map<String, Object>>> handleUnsupportedOperation(UnsupportedOperationException e) {
        log.warn("Not implemented: {}", e.getMessage());
        return Mono.just(ResponseEntity
            .status(HttpStatus.NOT_IMPLEMENTED)
            .body(Map.of(
                "error", "Not Implemented",
                "message", e.getMessage(),
                "timestamp", Instant.now().toString()
            )));
    }

    @ExceptionHandler(MaxUploadSizeExceededException.class)
    public Mono<ResponseEntity<Map<String, Object>>> handleMaxUploadSize(MaxUploadSizeExceededException e) {
        log.warn("File too large: {}", e.getMessage());
        return Mono.just(ResponseEntity
            .status(HttpStatus.PAYLOAD_TOO_LARGE)
            .body(Map.of(
                "error", "Payload Too Large",
                "message", "File size exceeds maximum allowed size",
                "timestamp", Instant.now().toString()
            )));
    }

    @ExceptionHandler(Exception.class)
    public Mono<ResponseEntity<Map<String, Object>>> handleGenericException(Exception e) {
        log.error("Unexpected error: {}", e.getMessage(), e);
        return Mono.just(ResponseEntity
            .status(HttpStatus.INTERNAL_SERVER_ERROR)
            .body(Map.of(
                "error", "Internal Server Error",
                "message", e.getMessage(),
                "timestamp", Instant.now().toString()
            )));
    }
}
