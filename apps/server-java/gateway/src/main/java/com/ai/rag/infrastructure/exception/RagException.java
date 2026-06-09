package com.ai.rag.infrastructure.exception;

/**
 * Application-level exception for RAG operations.
 * Wraps domain exceptions and provides user-friendly messages.
 */
public class RagException extends RuntimeException {

    public RagException(String message) {
        super(message);
    }

    public RagException(String message, Throwable cause) {
        super(message, cause);
    }

    /**
     * Factory method for not found errors.
     */
    public static RagException notFound(String message) {
        return new RagException("Not found: " + message);
    }

    /**
     * Factory method for validation errors.
     */
    public static RagException validation(String message) {
        return new RagException("Validation error: " + message);
    }

    /**
     * Factory method for processing errors.
     */
    public static RagException processing(String message, Throwable cause) {
        return new RagException("Processing error: " + message, cause);
    }
}
