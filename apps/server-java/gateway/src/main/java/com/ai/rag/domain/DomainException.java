package com.ai.rag.domain;

/**
 * Base exception for domain rule violations.
 * Indicates that a business rule was violated.
 */
public class DomainException extends RuntimeException {

    public DomainException(String message) {
        super(message);
    }

    public DomainException(String message, Throwable cause) {
        super(message, cause);
    }
}
