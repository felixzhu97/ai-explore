package com.ai.agents.domain;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("ModelVersion Tests")
class ModelVersionTest {

    @Nested
    @DisplayName("register factory method")
    class RegisterFactoryMethodTests {

        @Test
        @DisplayName("should create model version with all parameters")
        void shouldCreateModelVersionWithAllParameters() {
            ModelVersion model = ModelVersion.register("gpt-4", "1.0.0", "s3://models/gpt-4");

            assertThat(model.modelName()).isEqualTo("gpt-4");
            assertThat(model.version()).isEqualTo("1.0.0");
            assertThat(model.artifactUri()).isEqualTo("s3://models/gpt-4");
        }

        @Test
        @DisplayName("should set initial status to REGISTERED")
        void shouldSetInitialStatusToRegistered() {
            ModelVersion model = ModelVersion.register("test", "1.0", "uri");

            assertThat(model.status()).isEqualTo(ModelVersion.ModelStatus.REGISTERED);
        }

        @Test
        @DisplayName("should initialize with empty metrics")
        void shouldInitializeWithEmptyMetrics() {
            ModelVersion model = ModelVersion.register("test", "1.0", "uri");

            assertThat(model.metrics()).isEmpty();
        }

        @Test
        @DisplayName("should initialize with empty metadata")
        void shouldInitializeWithEmptyMetadata() {
            ModelVersion model = ModelVersion.register("test", "1.0", "uri");

            assertThat(model.metadata()).isEmpty();
        }

        @Test
        @DisplayName("should generate unique id")
        void shouldGenerateUniqueId() {
            ModelVersion model1 = ModelVersion.register("test", "1.0", "uri");
            ModelVersion model2 = ModelVersion.register("test", "1.0", "uri");

            assertThat(model1.id()).isNotEqualTo(model2.id());
        }

        @Test
        @DisplayName("should set created timestamp")
        void shouldSetCreatedTimestamp() {
            ModelVersion model = ModelVersion.register("test", "1.0", "uri");

            assertThat(model.createdAt()).isNotNull();
        }
    }

    @Nested
    @DisplayName("withMetrics method")
    class WithMetricsMethodTests {

        @Test
        @DisplayName("should add metrics to model version")
        void shouldAddMetricsToModelVersion() {
            ModelVersion model = ModelVersion.register("test", "1.0", "uri");
            Map<String, Object> metrics = Map.of("accuracy", 0.95, "latency", 100L);

            ModelVersion updated = model.withMetrics(metrics);

            assertThat(updated.getMetric("accuracy")).isEqualTo(0.95);
            assertThat(updated.getMetric("latency")).isEqualTo(100L);
        }

        @Test
        @DisplayName("should combine existing and new metrics")
        void shouldCombineExistingAndNewMetrics() {
            ModelVersion model = ModelVersion.register("test", "1.0", "uri")
                    .withMetrics(Map.of("existing", 1.0));
            Map<String, Object> newMetrics = Map.of("new", 2.0);

            ModelVersion updated = model.withMetrics(newMetrics);

            assertThat(updated.getMetric("existing")).isEqualTo(1.0);
            assertThat(updated.getMetric("new")).isEqualTo(2.0);
        }

        @Test
        @DisplayName("should not modify original model version")
        void shouldNotModifyOriginalModelVersion() {
            ModelVersion model = ModelVersion.register("test", "1.0", "uri");

            model.withMetrics(Map.of("key", 1.0));

            assertThat(model.metrics()).isEmpty();
        }
    }

    @Nested
    @DisplayName("withMetadata method")
    class WithMetadataMethodTests {

        @Test
        @DisplayName("should add metadata to model version")
        void shouldAddMetadataToModelVersion() {
            ModelVersion model = ModelVersion.register("test", "1.0", "uri");
            Map<String, String> metadata = Map.of("author", "team", "env", "prod");

            ModelVersion updated = model.withMetadata(metadata);

            assertThat(updated.getMetadata("author")).isEqualTo("team");
            assertThat(updated.getMetadata("env")).isEqualTo("prod");
        }

        @Test
        @DisplayName("should combine existing and new metadata")
        void shouldCombineExistingAndNewMetadata() {
            ModelVersion model = ModelVersion.register("test", "1.0", "uri")
                    .withMetadata(Map.of("existing", "value"));
            Map<String, String> newMetadata = Map.of("new", "data");

            ModelVersion updated = model.withMetadata(newMetadata);

            assertThat(updated.getMetadata("existing")).isEqualTo("value");
            assertThat(updated.getMetadata("new")).isEqualTo("data");
        }
    }

    @Nested
    @DisplayName("stage method")
    class StageMethodTests {

        @Test
        @DisplayName("should change model status")
        void shouldChangeModelStatus() {
            ModelVersion model = ModelVersion.register("test", "1.0", "uri");

            ModelVersion staged = model.stage(ModelVersion.ModelStatus.STAGING);

            assertThat(staged.status()).isEqualTo(ModelVersion.ModelStatus.STAGING);
        }

        @Test
        @DisplayName("should preserve other properties")
        void shouldPreserveOtherProperties() {
            ModelVersion model = ModelVersion.register("test", "1.0", "uri");

            ModelVersion staged = model.stage(ModelVersion.ModelStatus.STAGING);

            assertThat(staged.modelName()).isEqualTo("test");
            assertThat(staged.version()).isEqualTo("1.0");
        }
    }

    @Nested
    @DisplayName("archive method")
    class ArchiveMethodTests {

        @Test
        @DisplayName("should set status to ARCHIVED")
        void shouldSetStatusToArchived() {
            ModelVersion model = ModelVersion.register("test", "1.0", "uri");

            ModelVersion archived = model.archive();

            assertThat(archived.status()).isEqualTo(ModelVersion.ModelStatus.ARCHIVED);
        }
    }

    @Nested
    @DisplayName("status checking methods")
    class StatusCheckingMethodTests {

        @Test
        @DisplayName("isActive should return true for PRODUCTION status")
        void isActiveShouldReturnTrueForProductionStatus() {
            ModelVersion model = ModelVersion.register("test", "1.0", "uri")
                    .stage(ModelVersion.ModelStatus.PRODUCTION);

            assertThat(model.isActive()).isTrue();
        }

        @Test
        @DisplayName("isActive should return true for STAGING status")
        void isActiveShouldReturnTrueForStagingStatus() {
            ModelVersion model = ModelVersion.register("test", "1.0", "uri")
                    .stage(ModelVersion.ModelStatus.STAGING);

            assertThat(model.isActive()).isTrue();
        }

        @Test
        @DisplayName("isActive should return false for REGISTERED status")
        void isActiveShouldReturnFalseForRegisteredStatus() {
            ModelVersion model = ModelVersion.register("test", "1.0", "uri");

            assertThat(model.isActive()).isFalse();
        }

        @Test
        @DisplayName("isProduction should return true for PRODUCTION status")
        void isProductionShouldReturnTrueForProductionStatus() {
            ModelVersion model = ModelVersion.register("test", "1.0", "uri")
                    .stage(ModelVersion.ModelStatus.PRODUCTION);

            assertThat(model.isProduction()).isTrue();
        }

        @Test
        @DisplayName("isProduction should return false for other statuses")
        void isProductionShouldReturnFalseForOtherStatuses() {
            ModelVersion model = ModelVersion.register("test", "1.0", "uri");

            assertThat(model.isProduction()).isFalse();
        }
    }
}
