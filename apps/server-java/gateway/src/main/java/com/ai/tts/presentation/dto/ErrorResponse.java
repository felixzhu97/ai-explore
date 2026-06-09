package com.ai.tts.presentation.dto;

public record ErrorResponse(
    String error,
    String message,
    Object details
) {
    public static ErrorResponse of(String error, String message) {
        return new ErrorResponse(error, message, null);
    }

    public static ErrorResponse of(String error, String message, Object details) {
        return new ErrorResponse(error, message, details);
    }
}
