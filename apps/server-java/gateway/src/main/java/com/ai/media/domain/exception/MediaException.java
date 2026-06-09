package com.ai.media.domain.exception;

/**
 * Base domain exception for media-related errors.
 */
public class MediaException extends RuntimeException {

    private final String errorCode;

    public MediaException(String message) {
        super(message);
        this.errorCode = "MEDIA_ERROR";
    }

    public MediaException(String message, Throwable cause) {
        super(message, cause);
        this.errorCode = "MEDIA_ERROR";
    }

    public MediaException(String errorCode, String message) {
        super(message);
        this.errorCode = errorCode;
    }

    public MediaException(String errorCode, String message, Throwable cause) {
        super(message, cause);
        this.errorCode = errorCode;
    }

    public String errorCode() {
        return errorCode;
    }

    public static class GenerationException extends MediaException {
        public GenerationException(String message) {
            super("GENERATION_ERROR", message);
        }

        public GenerationException(String message, Throwable cause) {
            super("GENERATION_ERROR", message, cause);
        }
    }

    public static class ProviderUnavailableException extends MediaException {
        public ProviderUnavailableException(String message) {
            super("PROVIDER_UNAVAILABLE", message);
        }

        public ProviderUnavailableException(String message, Throwable cause) {
            super("PROVIDER_UNAVAILABLE", message, cause);
        }
    }

    public static class InvalidParamsException extends MediaException {
        public InvalidParamsException(String message) {
            super("INVALID_PARAMS", message);
        }
    }
}
