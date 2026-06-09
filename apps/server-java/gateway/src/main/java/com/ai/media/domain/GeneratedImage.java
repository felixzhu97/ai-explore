package com.ai.media.domain;

import java.util.List;
import java.util.Objects;

/**
 * Value object representing a generated image with metadata.
 */
public final class GeneratedImage {
    private final String base64Data;
    private final String format;
    private final int width;
    private final int height;
    private final long seed;

    private GeneratedImage(Builder builder) {
        this.base64Data = builder.base64Data;
        this.format = builder.format != null ? builder.format : "png";
        this.width = builder.width;
        this.height = builder.height;
        this.seed = builder.seed;
    }

    public static GeneratedImage of(String base64Data) {
        return builder().base64Data(base64Data).build();
    }

    public static GeneratedImage of(String base64Data, long seed) {
        return builder().base64Data(base64Data).seed(seed).build();
    }

    public static Builder builder() {
        return new Builder();
    }

    public String base64Data() {
        return base64Data;
    }

    public String format() {
        return format;
    }

    public int width() {
        return width;
    }

    public int height() {
        return height;
    }

    public long seed() {
        return seed;
    }

    public String dataUrl() {
        return "data:image/" + format + ";base64," + base64Data;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        GeneratedImage that = (GeneratedImage) o;
        return seed == that.seed &&
                width == that.width &&
                height == that.height &&
                Objects.equals(base64Data, that.base64Data) &&
                Objects.equals(format, that.format);
    }

    @Override
    public int hashCode() {
        return Objects.hash(base64Data, format, width, height, seed);
    }

    public static final class Builder {
        private String base64Data;
        private String format;
        private int width;
        private int height;
        private long seed;

        public Builder base64Data(String base64Data) {
            this.base64Data = base64Data;
            return this;
        }

        public Builder format(String format) {
            this.format = format;
            return this;
        }

        public Builder width(int width) {
            this.width = width;
            return this;
        }

        public Builder height(int height) {
            this.height = height;
            return this;
        }

        public Builder seed(long seed) {
            this.seed = seed;
            return this;
        }

        public GeneratedImage build() {
            if (base64Data == null || base64Data.isBlank()) {
                throw new IllegalArgumentException("Base64 data is required");
            }
            return new GeneratedImage(this);
        }
    }
}
