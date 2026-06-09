package com.ai.vision.domain;

import java.util.Objects;
import java.util.UUID;

/**
 * Value object representing a unique identifier for an Image aggregate.
 * Immutable and thread-safe.
 */
public final class ImageId {

    private final String value;

    private ImageId(String value) {
        this.value = value;
    }

    public static ImageId generate() {
        return new ImageId(UUID.randomUUID().toString());
    }

    public static ImageId of(String value) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("ImageId cannot be null or blank");
        }
        return new ImageId(value);
    }

    public String value() {
        return value;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ImageId imageId = (ImageId) o;
        return Objects.equals(value, imageId.value);
    }

    @Override
    public int hashCode() {
        return Objects.hash(value);
    }

    @Override
    public String toString() {
        return value;
    }
}
