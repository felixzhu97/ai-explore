package com.ai.media.domain;

import java.util.Objects;

/**
 * Value object representing LoRA (Low-Rank Adaptation) information.
 */
public final class LoraInfo {
    private final String id;
    private final String name;
    private final String description;
    private final String triggerWord;
    private final boolean isInstalled;

    private LoraInfo(Builder builder) {
        this.id = builder.id;
        this.name = builder.name;
        this.description = builder.description;
        this.triggerWord = builder.triggerWord;
        this.isInstalled = builder.isInstalled;
    }

    public static LoraInfo of(String id, String name) {
        return builder().id(id).name(name).isInstalled(true).build();
    }

    public static LoraInfo of(String id, String name, boolean isInstalled) {
        return builder().id(id).name(name).isInstalled(isInstalled).build();
    }

    public static Builder builder() {
        return new Builder();
    }

    public String id() {
        return id;
    }

    public String name() {
        return name;
    }

    public String description() {
        return description;
    }

    public String triggerWord() {
        return triggerWord;
    }

    public boolean isInstalled() {
        return isInstalled;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        LoraInfo loraInfo = (LoraInfo) o;
        return isInstalled == loraInfo.isInstalled &&
                Objects.equals(id, loraInfo.id) &&
                Objects.equals(name, loraInfo.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, name, isInstalled);
    }

    public static final class Builder {
        private String id;
        private String name;
        private String description;
        private String triggerWord;
        private boolean isInstalled;

        public Builder id(String id) {
            this.id = id;
            return this;
        }

        public Builder name(String name) {
            this.name = name;
            return this;
        }

        public Builder description(String description) {
            this.description = description;
            return this;
        }

        public Builder triggerWord(String triggerWord) {
            this.triggerWord = triggerWord;
            return this;
        }

        public Builder isInstalled(boolean isInstalled) {
            this.isInstalled = isInstalled;
            return this;
        }

        public LoraInfo build() {
            if (id == null || id.isBlank()) {
                throw new IllegalArgumentException("LoRA ID is required");
            }
            return new LoraInfo(this);
        }
    }
}
