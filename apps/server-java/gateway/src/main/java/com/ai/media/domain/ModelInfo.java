package com.ai.media.domain;

import java.util.List;
import java.util.Objects;

/**
 * Value object representing model information.
 */
public final class ModelInfo {
    private final String id;
    private final String name;
    private final String type;
    private final String description;
    private final List<String> supportedTasks;
    private final boolean isDefault;

    private ModelInfo(Builder builder) {
        this.id = builder.id;
        this.name = builder.name;
        this.type = builder.type;
        this.description = builder.description;
        this.supportedTasks = builder.supportedTasks != null 
                ? List.copyOf(builder.supportedTasks) 
                : List.of("text-to-image");
        this.isDefault = builder.isDefault;
    }

    public static ModelInfo of(String id, String name, String type) {
        return builder().id(id).name(name).type(type).build();
    }

    public static ModelInfo of(String id, String name, String type, boolean isDefault) {
        return builder().id(id).name(name).type(type).isDefault(isDefault).build();
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

    public String type() {
        return type;
    }

    public String description() {
        return description;
    }

    public List<String> supportedTasks() {
        return supportedTasks;
    }

    public boolean isDefault() {
        return isDefault;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ModelInfo modelInfo = (ModelInfo) o;
        return isDefault == modelInfo.isDefault &&
                Objects.equals(id, modelInfo.id) &&
                Objects.equals(name, modelInfo.name) &&
                Objects.equals(type, modelInfo.type);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, name, type, isDefault);
    }

    public static final class Builder {
        private String id;
        private String name;
        private String type;
        private String description;
        private List<String> supportedTasks;
        private boolean isDefault;

        public Builder id(String id) {
            this.id = id;
            return this;
        }

        public Builder name(String name) {
            this.name = name;
            return this;
        }

        public Builder type(String type) {
            this.type = type;
            return this;
        }

        public Builder description(String description) {
            this.description = description;
            return this;
        }

        public Builder supportedTasks(List<String> supportedTasks) {
            this.supportedTasks = supportedTasks;
            return this;
        }

        public Builder isDefault(boolean isDefault) {
            this.isDefault = isDefault;
            return this;
        }

        public ModelInfo build() {
            if (id == null || id.isBlank()) {
                throw new IllegalArgumentException("Model ID is required");
            }
            return new ModelInfo(this);
        }
    }
}
