package com.ai.agents.domain;

import java.time.Instant;
import java.util.*;

/**
 * Feature vector value object.
 * Represents a feature store feature vector with values and metadata.
 */
public final class FeatureVector {
    private final FeatureVectorId id;
    private final String featureName;
    private final Map<String, Object> features;
    private final String entityId;
    private final Instant timestamp;
    private final Map<String, String> metadata;

    private FeatureVector(
            FeatureVectorId id,
            String featureName,
            Map<String, Object> features,
            String entityId,
            Instant timestamp,
            Map<String, String> metadata
    ) {
        this.id = Objects.requireNonNull(id, "FeatureVectorId cannot be null");
        this.featureName = Objects.requireNonNull(featureName, "FeatureName cannot be null");
        this.features = features != null ? Map.copyOf(features) : Map.of();
        this.entityId = entityId;
        this.timestamp = timestamp != null ? timestamp : Instant.now();
        this.metadata = metadata != null ? Map.copyOf(metadata) : Map.of();
    }

    public static FeatureVector create(String featureName, String entityId, Map<String, Object> features) {
        return new FeatureVector(
                FeatureVectorId.generate(),
                featureName,
                features,
                entityId,
                Instant.now(),
                Map.of()
        );
    }

    public FeatureVector withMetadata(String key, String value) {
        Map<String, String> newMeta = new HashMap<>(metadata);
        newMeta.put(key, value);
        return new FeatureVector(id, featureName, features, entityId, timestamp, newMeta);
    }

    public Object getFeature(String key) { return features.get(key); }
    public String getMetadata(String key) { return metadata.get(key); }

    public FeatureVectorId id() { return id; }
    public String featureName() { return featureName; }
    public Map<String, Object> features() { return features; }
    public String entityId() { return entityId; }
    public Instant timestamp() { return timestamp; }
    public Map<String, String> metadata() { return metadata; }

    private static final class FeatureVectorId {
        private final String value;

        private FeatureVectorId(String value) { this.value = value; }

        static FeatureVectorId generate() {
            return new FeatureVectorId(UUID.randomUUID().toString());
        }

        String value() { return value; }
    }
}
