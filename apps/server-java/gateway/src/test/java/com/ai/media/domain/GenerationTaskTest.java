package com.ai.media.domain;

import com.ai.media.domain.exception.MediaException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("GenerationTask Tests")
class GenerationTaskTest {

    @Nested
    @DisplayName("create factory method")
    class CreateFactoryMethodTests {

        @Test
        @DisplayName("should create task with PENDING status")
        void shouldCreateTaskWithPendingStatus() {
            GenerationParams params = GenerationParams.of("A cat");

            GenerationTask task = GenerationTask.create(params);

            assertThat(task.status()).isEqualTo(GenerationTask.Status.PENDING);
        }

        @Test
        @DisplayName("should store generation params")
        void shouldStoreGenerationParams() {
            GenerationParams params = GenerationParams.of("A sunset");

            GenerationTask task = GenerationTask.create(params);

            assertThat(task.params()).isEqualTo(params);
        }

        @Test
        @DisplayName("should generate unique id")
        void shouldGenerateUniqueId() {
            GenerationParams params = GenerationParams.of("Test");
            GenerationTask task1 = GenerationTask.create(params);
            GenerationTask task2 = GenerationTask.create(params);

            assertThat(task1.id()).isNotEqualTo(task2.id());
        }

        @Test
        @DisplayName("should reject null params")
        void shouldRejectNullParams() {
            assertThatThrownBy(() -> GenerationTask.create(null))
                    .isInstanceOf(MediaException.InvalidParamsException.class);
        }
    }

    @Nested
    @DisplayName("canGenerate method")
    class CanGenerateMethodTests {

        @Test
        @DisplayName("should return true for PENDING task")
        void shouldReturnTrueForPendingTask() {
            GenerationTask task = GenerationTask.create(GenerationParams.of("Test"));

            assertThat(task.canGenerate()).isTrue();
        }

        @Test
        @DisplayName("should return false for non-PENDING task")
        void shouldReturnFalseForNonPendingTask() {
            GenerationTask task = GenerationTask.create(GenerationParams.of("Test"));
            task.startGeneration();

            assertThat(task.canGenerate()).isFalse();
        }
    }

    @Nested
    @DisplayName("startGeneration method")
    class StartGenerationMethodTests {

        @Test
        @DisplayName("should transition to GENERATING status")
        void shouldTransitionToGeneratingStatus() {
            GenerationTask task = GenerationTask.create(GenerationParams.of("Test"));

            task.startGeneration();

            assertThat(task.status()).isEqualTo(GenerationTask.Status.GENERATING);
        }

        @Test
        @DisplayName("should set startedAt timestamp")
        void shouldSetStartedAtTimestamp() {
            GenerationTask task = GenerationTask.create(GenerationParams.of("Test"));

            task.startGeneration();

            assertThat(task.startedAt()).isNotNull();
        }

        @Test
        @DisplayName("should reject when not in PENDING status")
        void shouldRejectWhenNotInPendingStatus() {
            GenerationTask task = GenerationTask.create(GenerationParams.of("Test"));
            task.startGeneration();

            assertThatThrownBy(() -> task.startGeneration())
                    .isInstanceOf(MediaException.GenerationException.class);
        }
    }

    @Nested
    @DisplayName("markCompleted method")
    class MarkCompletedMethodTests {

        @Test
        @DisplayName("should transition to COMPLETED status")
        void shouldTransitionToCompletedStatus() {
            GenerationTask task = GenerationTask.create(GenerationParams.of("Test"));
            GeneratedImage image = GeneratedImage.of("base64data123", 12345L);

            task.markCompleted(List.of(image));

            assertThat(task.status()).isEqualTo(GenerationTask.Status.COMPLETED);
        }

        @Test
        @DisplayName("should store generated images")
        void shouldStoreGeneratedImages() {
            GenerationTask task = GenerationTask.create(GenerationParams.of("Test"));
            GeneratedImage image = GeneratedImage.of("base64data", 12345L);

            task.markCompleted(List.of(image));

            assertThat(task.images()).hasSize(1);
            assertThat(task.images().get(0).base64Data()).isEqualTo("base64data");
        }

        @Test
        @DisplayName("should reject null images list")
        void shouldRejectNullImagesList() {
            GenerationTask task = GenerationTask.create(GenerationParams.of("Test"));

            assertThatThrownBy(() -> task.markCompleted(null))
                    .isInstanceOf(MediaException.InvalidParamsException.class);
        }

        @Test
        @DisplayName("should reject empty images list")
        void shouldRejectEmptyImagesList() {
            GenerationTask task = GenerationTask.create(GenerationParams.of("Test"));

            assertThatThrownBy(() -> task.markCompleted(List.of()))
                    .isInstanceOf(MediaException.InvalidParamsException.class);
        }
    }

    @Nested
    @DisplayName("markFailed method")
    class MarkFailedMethodTests {

        @Test
        @DisplayName("should transition to FAILED status")
        void shouldTransitionToFailedStatus() {
            GenerationTask task = GenerationTask.create(GenerationParams.of("Test"));

            task.markFailed("Network error");

            assertThat(task.status()).isEqualTo(GenerationTask.Status.FAILED);
        }

        @Test
        @DisplayName("should store failure reason")
        void shouldStoreFailureReason() {
            GenerationTask task = GenerationTask.create(GenerationParams.of("Test"));

            task.markFailed("API timeout");

            assertThat(task.failureReason()).isEqualTo("API timeout");
        }

        @Test
        @DisplayName("should reject null reason")
        void shouldRejectNullReason() {
            GenerationTask task = GenerationTask.create(GenerationParams.of("Test"));

            assertThatThrownBy(() -> task.markFailed(null))
                    .isInstanceOf(MediaException.InvalidParamsException.class);
        }

        @Test
        @DisplayName("should reject blank reason")
        void shouldRejectBlankReason() {
            GenerationTask task = GenerationTask.create(GenerationParams.of("Test"));

            assertThatThrownBy(() -> task.markFailed("   "))
                    .isInstanceOf(MediaException.InvalidParamsException.class);
        }
    }

    @Nested
    @DisplayName("status checking methods")
    class StatusCheckingMethodTests {

        @Test
        @DisplayName("isCompleted should return true for COMPLETED status")
        void isCompletedShouldReturnTrueForCompletedStatus() {
            GenerationTask task = GenerationTask.create(GenerationParams.of("Test"));
            task.markCompleted(List.of(GeneratedImage.of("base64data", 12345L)));

            assertThat(task.isCompleted()).isTrue();
        }

        @Test
        @DisplayName("isFailed should return true for FAILED status")
        void isFailedShouldReturnTrueForFailedStatus() {
            GenerationTask task = GenerationTask.create(GenerationParams.of("Test"));
            task.markFailed("Error");

            assertThat(task.isFailed()).isTrue();
        }

        @Test
        @DisplayName("isPending should return true for PENDING status")
        void isPendingShouldReturnTrueForPendingStatus() {
            GenerationTask task = GenerationTask.create(GenerationParams.of("Test"));

            assertThat(task.isPending()).isTrue();
        }

        @Test
        @DisplayName("isGenerating should return true for GENERATING status")
        void isGeneratingShouldReturnTrueForGeneratingStatus() {
            GenerationTask task = GenerationTask.create(GenerationParams.of("Test"));
            task.startGeneration();

            assertThat(task.isGenerating()).isTrue();
        }
    }

    @Nested
    @DisplayName("processingTimeMs method")
    class ProcessingTimeMsMethodTests {

        @Test
        @DisplayName("should return 0 when not started")
        void shouldReturn0WhenNotStarted() {
            GenerationTask task = GenerationTask.create(GenerationParams.of("Test"));

            assertThat(task.processingTimeMs()).isZero();
        }

        @Test
        @DisplayName("should calculate processing time")
        void shouldCalculateProcessingTime() {
            GenerationTask task = GenerationTask.create(GenerationParams.of("Test"));
            task.startGeneration();
            task.markCompleted(List.of(GeneratedImage.of("base64data", 12345L)));

            // Processing time should be >= 0 since it may complete almost instantly
            assertThat(task.processingTimeMs()).isGreaterThanOrEqualTo(0);
        }
    }

    @Nested
    @DisplayName("Status enum values")
    class StatusEnumTests {

        @Test
        @DisplayName("should have all expected status values")
        void shouldHaveAllExpectedStatusValues() {
            assertThat(GenerationTask.Status.values()).containsExactlyInAnyOrder(
                    GenerationTask.Status.PENDING,
                    GenerationTask.Status.GENERATING,
                    GenerationTask.Status.COMPLETED,
                    GenerationTask.Status.FAILED
            );
        }
    }
}
