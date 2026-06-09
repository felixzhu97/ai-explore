package com.ai.tts.domain.exception;

public class TtsDomainException extends RuntimeException {

    private final String errorCode;

    private TtsDomainException(String message, String errorCode, Throwable cause) {
        super(message, cause);
        this.errorCode = errorCode;
    }

    public String errorCode() {
        return errorCode;
    }

    public static TtsDomainException invalidRequest(String message) {
        return new TtsDomainException(message, "INVALID_REQUEST", null);
    }

    public static TtsDomainException synthesisFailed(String message, Throwable cause) {
        return new TtsDomainException("Speech synthesis failed: " + message, "SYNTHESIS_FAILED", cause);
    }

    public static TtsDomainException providerNotAvailable(String provider, Throwable cause) {
        return new TtsDomainException(
            "TTS provider not available: " + provider,
            "PROVIDER_NOT_AVAILABLE",
            cause
        );
    }

    public static TtsDomainException audioGenerationFailed(Throwable cause) {
        return new TtsDomainException(
            "Audio generation failed: " + cause.getMessage(),
            "AUDIO_GENERATION_FAILED",
            cause
        );
    }
}
