package com.ai.media.domain;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Objects;

/**
 * Value object representing immutable image generation parameters.
 * Contains all parameters needed for text-to-image generation.
 */
public final class GenerationParams {
    private final String prompt;
    private final String negativePrompt;
    private final int width;
    private final int height;
    private final int steps;
    private final float cfgScale;
    private final Long seed;
    private final String model;
    private final String lora;

    private GenerationParams(Builder builder) {
        this.prompt = builder.prompt;
        this.negativePrompt = builder.negativePrompt != null 
                ? builder.negativePrompt 
                : "blurry, ugly, distorted, low quality, watermark, text, signature";
        this.width = builder.width > 0 ? builder.width : 512;
        this.height = builder.height > 0 ? builder.height : 512;
        this.steps = builder.steps > 0 ? builder.steps : 25;
        this.cfgScale = builder.cfgScale > 0 ? builder.cfgScale : 7.5f;
        this.seed = builder.seed;
        this.model = builder.model;
        this.lora = builder.lora;
    }

    public static GenerationParams of(String prompt) {
        return builder().prompt(prompt).build();
    }

    public static Builder builder() {
        return new Builder();
    }

    public String prompt() {
        return prompt;
    }

    @JsonProperty("negative_prompt")
    public String negativePrompt() {
        return negativePrompt;
    }

    public int width() {
        return width;
    }

    public int height() {
        return height;
    }

    public int steps() {
        return steps;
    }

    @JsonProperty("cfg_scale")
    public float cfgScale() {
        return cfgScale;
    }

    public Long seed() {
        return seed;
    }

    public String model() {
        return model;
    }

    public String lora() {
        return lora;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        GenerationParams that = (GenerationParams) o;
        return width == that.width &&
                height == that.height &&
                steps == that.steps &&
                Float.compare(that.cfgScale, cfgScale) == 0 &&
                Objects.equals(prompt, that.prompt) &&
                Objects.equals(negativePrompt, that.negativePrompt) &&
                Objects.equals(seed, that.seed) &&
                Objects.equals(model, that.model) &&
                Objects.equals(lora, that.lora);
    }

    @Override
    public int hashCode() {
        return Objects.hash(prompt, negativePrompt, width, height, steps, cfgScale, seed, model, lora);
    }

    public static final class Builder {
        private String prompt;
        private String negativePrompt;
        private int width;
        private int height;
        private int steps;
        private float cfgScale;
        private Long seed;
        private String model;
        private String lora;

        public Builder prompt(String prompt) {
            this.prompt = prompt;
            return this;
        }

        public Builder negativePrompt(String negativePrompt) {
            this.negativePrompt = negativePrompt;
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

        public Builder steps(int steps) {
            this.steps = steps;
            return this;
        }

        public Builder cfgScale(float cfgScale) {
            this.cfgScale = cfgScale;
            return this;
        }

        public Builder seed(Long seed) {
            this.seed = seed;
            return this;
        }

        public Builder model(String model) {
            this.model = model;
            return this;
        }

        public Builder lora(String lora) {
            this.lora = lora;
            return this;
        }

        public GenerationParams build() {
            if (prompt == null || prompt.isBlank()) {
                throw new IllegalArgumentException("Prompt is required");
            }
            return new GenerationParams(this);
        }
    }
}
