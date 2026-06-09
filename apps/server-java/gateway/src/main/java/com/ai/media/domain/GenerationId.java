package com.ai.media.domain;

import java.util.UUID;

/**
 * Value object representing a unique generation task identifier.
 * Immutable and thread-safe.
 */
public final class GenerationId {
    private final String value;

    private GenerationId(String value) {
        this.value = value;
    }

    public static GenerationId generate() {
        return new GenerationId(UUID.randomUUID().toString());
    }

    public static GenerationId of(String value) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("GenerationId cannot be null or blank");
        }
        return new GenerationId(value);
    }

    public String value() {
        return value;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        GenerationId that = (GenerationId) o;
        return value.equals(that.value);
    }

    @Override
    public int hashCode() {
        return value.hashCode();
    }

    @Override
    public String toString() {
        return value;
    }
}
