package com.ai.tts.infrastructure.exception;

import com.ai.tts.presentation.dto.ErrorResponse;
import com.ai.tts.domain.exception.TtsDomainException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class TtsExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(TtsExceptionHandler.class);

    @ExceptionHandler(TtsDomainException.class)
    public ResponseEntity<ErrorResponse> handleDomainException(TtsDomainException e) {
        log.error("TTS domain error: {} [{}]", e.getMessage(), e.errorCode(), e);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ErrorResponse.of(e.errorCode(), e.getMessage()));
    }

    @ExceptionHandler(UnsupportedOperationException.class)
    public ResponseEntity<ErrorResponse> handleUnsupportedOperation(UnsupportedOperationException e) {
        log.warn("Unsupported operation: {}", e.getMessage());
        return ResponseEntity.status(HttpStatus.NOT_IMPLEMENTED)
                .body(ErrorResponse.of("NOT_IMPLEMENTED", e.getMessage()));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGenericException(Exception e) {
        log.error("Unexpected error: {}", e.getMessage(), e);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ErrorResponse.of("INTERNAL_ERROR", "An unexpected error occurred"));
    }
}
