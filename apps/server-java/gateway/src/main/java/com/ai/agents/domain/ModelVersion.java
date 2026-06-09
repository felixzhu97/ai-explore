package com.ai.agents.domain;

import java.time.Instant;
import java.util.*;

/**
 * Model version value object.
 * Represents an ML model version with metadata.
 */
public final class ModelVersion {
    private final String id;
    private final String modelName;
    private final String version;
    private final ModelStatus status;
    private final String artifactUri;
    private final Map<String, Object> metrics;
    private final Map<String, String> metadata;
    private final Instant createdAt;
    private final String createdBy;

    private ModelVersion(
            String id,
            String modelName,
            String version,
            ModelStatus status,
            String artifactUri,
            Map<String, Object> metrics,
            Map<String, String> metadata,
            Instant createdAt,
            String createdBy
    ) {
        this.id = Objects.requireNonNull(id, "Id cannot be null");
        this.modelName = Objects.requireNonNull(modelName, "ModelName cannot be null");
        this.version = Objects.requireNonNull(version, "Version cannot be null");
        this.status = Objects.requireNonNull(status, "Status cannot be null");
        this.artifactUri = artifactUri;
        this.metrics = metrics != null ? Map.copyOf(metrics) : Map.of();
        this.metadata = metadata != null ? Map.copyOf(metadata) : Map.of();
        this.createdAt = Objects.requireNonNull(createdAt, "CreatedAt cannot be null");
        this.createdBy = createdBy;
    }

    public static ModelVersion register(String modelName, String version, String artifactUri) {
        return new ModelVersion(
                UUID.randomUUID().toString(),
                modelName,
                version,
                ModelStatus.REGISTERED,
                artifactUri,
                Map.of(),
                Map.of(),
                Instant.now(),
                null
        );
    }

    public ModelVersion withMetrics(Map<String, Object> newMetrics) {
        Map<String, Object> combined = new HashMap<>(metrics);
        combined.putAll(newMetrics);
        return new ModelVersion(
                id, modelName, version, status, artifactUri,
                combined, metadata, createdAt, createdBy
        );
    }

    public ModelVersion withMetadata(Map<String, String> newMetadata) {
        Map<String, String> combined = new HashMap<>(metadata);
        combined.putAll(newMetadata);
        return new ModelVersion(
                id, modelName, version, status, artifactUri,
                metrics, combined, createdAt, createdBy
        );
    }

    public ModelVersion stage(ModelStatus newStatus) {
        return new ModelVersion(
                id, modelName, version, newStatus, artifactUri,
                metrics, metadata, createdAt, createdBy
        );
    }

    public ModelVersion archive() {
        return stage(ModelStatus.ARCHIVED);
    }

    public String id() { return id; }
    public String idValue() { return id; }
    public String modelName() { return modelName; }
    public String version() { return version; }
    public ModelStatus status() { return status; }
    public String artifactUri() { return artifactUri; }
    public Map<String, Object> metrics() { return metrics; }
    public Map<String, String> metadata() { return metadata; }
    public Instant createdAt() { return createdAt; }
    public String createdBy() { return createdBy; }

    public boolean isActive() { return status == ModelStatus.PRODUCTION || status == ModelStatus.STAGING; }
    public boolean isProduction() { return status == ModelStatus.PRODUCTION; }
    public Object getMetric(String key) { return metrics.get(key); }
    public String getMetadata(String key) { return metadata.get(key); }

    public enum ModelStatus {
        REGISTERED, STAGING, PRODUCTION, DEPRECATED, ARCHIVED
    }
}
