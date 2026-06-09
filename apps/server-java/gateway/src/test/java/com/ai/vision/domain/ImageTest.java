package com.ai.vision.domain;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("Image Tests")
class ImageTest {

    private ImageData createTestImageData() {
        byte[] data = new byte[]{1, 2, 3, 4, 5};
        return ImageData.of(data, "image/png");
    }

    @Nested
    @DisplayName("create factory method")
    class CreateFactoryMethodTests {

        @Test
        @DisplayName("should create image with PENDING state")
        void shouldCreateImageWithPendingState() {
            ImageData data = createTestImageData();

            Image image = Image.create(data);

            assertThat(image.state()).isEqualTo(Image.State.PENDING);
        }

        @Test
        @DisplayName("should store image data")
        void shouldStoreImageData() {
            ImageData data = createTestImageData();

            Image image = Image.create(data);

            assertThat(image.data()).isEqualTo(data);
        }

        @Test
        @DisplayName("should generate unique id")
        void shouldGenerateUniqueId() {
            ImageData data = createTestImageData();
            Image image1 = Image.create(data);
            Image image2 = Image.create(data);

            assertThat(image1.id()).isNotEqualTo(image2.id());
        }

        @Test
        @DisplayName("should reject null image data")
        void shouldRejectNullImageData() {
            assertThatThrownBy(() -> Image.create(null))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("Image data cannot be null or empty");
        }

        @Test
        @DisplayName("should reject empty image data")
        void shouldRejectEmptyImageData() {
            ImageData emptyData = ImageData.of(new byte[]{});

            assertThatThrownBy(() -> Image.create(emptyData))
                    .isInstanceOf(IllegalArgumentException.class);
        }
    }

    @Nested
    @DisplayName("state checking methods")
    class StateCheckingMethodTests {

        @Test
        @DisplayName("isPending should return true for PENDING state")
        void isPendingShouldReturnTrueForPendingState() {
            Image image = Image.create(createTestImageData());

            assertThat(image.isPending()).isTrue();
        }

        @Test
        @DisplayName("isAnalyzing should return true for ANALYZING state")
        void isAnalyzingShouldReturnTrueForAnalyzingState() {
            Image image = Image.create(createTestImageData());
            image.beginDetection(0.5f);

            assertThat(image.isAnalyzing()).isTrue();
        }

        @Test
        @DisplayName("isCompleted should return true for COMPLETED state")
        void isCompletedShouldReturnTrueForCompletedState() {
            Image image = Image.create(createTestImageData());
            DetectionResult result = DetectionResult.of(
                java.util.List.of(new DetectionResult.DetectedObject("person", 0.9f, 
                    new DetectionResult.BoundingBox(0, 0, 100, 100)))
            );
            image.beginDetection(0.5f);
            image.completeDetection(result);

            assertThat(image.isCompleted()).isTrue();
        }

        @Test
        @DisplayName("isFailed should return true for FAILED state")
        void isFailedShouldReturnTrueForFailedState() {
            Image image = Image.create(createTestImageData());
            image.beginDetection(0.5f);
            image.failDetection("Error");

            assertThat(image.isFailed()).isTrue();
        }
    }

    @Nested
    @DisplayName("canTransitionTo method")
    class CanTransitionToMethodTests {

        @Test
        @DisplayName("PENDING can transition to ANALYZING")
        void pendingCanTransitionToAnalyzing() {
            Image image = Image.create(createTestImageData());

            assertThat(image.canTransitionTo(Image.State.ANALYZING)).isTrue();
        }

        @Test
        @DisplayName("PENDING cannot transition to COMPLETED")
        void pendingCannotTransitionToCompleted() {
            Image image = Image.create(createTestImageData());

            assertThat(image.canTransitionTo(Image.State.COMPLETED)).isFalse();
        }

        @Test
        @DisplayName("ANALYZING can transition to COMPLETED or FAILED")
        void analyzingCanTransitionToCompletedOrFailed() {
            Image image = Image.create(createTestImageData());
            image.beginDetection(0.5f);

            assertThat(image.canTransitionTo(Image.State.COMPLETED)).isTrue();
            assertThat(image.canTransitionTo(Image.State.FAILED)).isTrue();
        }

        @Test
        @DisplayName("FAILED is terminal state")
        void failedIsTerminalState() {
            Image image = Image.create(createTestImageData());
            image.beginDetection(0.5f);
            image.failDetection("Error");

            assertThat(image.canTransitionTo(Image.State.PENDING)).isFalse();
            assertThat(image.canTransitionTo(Image.State.ANALYZING)).isFalse();
        }
    }

    @Nested
    @DisplayName("detection workflow")
    class DetectionWorkflowTests {

        @Test
        @DisplayName("should complete detection workflow")
        void shouldCompleteDetectionWorkflow() {
            Image image = Image.create(createTestImageData());
            DetectionResult result = DetectionResult.of(
                java.util.List.of(new DetectionResult.DetectedObject("person", 0.95f,
                    new DetectionResult.BoundingBox(0, 0, 100, 100)))
            );

            image.beginDetection(0.5f);
            image.completeDetection(result);

            assertThat(image.isCompleted()).isTrue();
            assertThat(image.detectionResult()).isPresent();
        }

        @Test
        @DisplayName("should fail detection")
        void shouldFailDetection() {
            Image image = Image.create(createTestImageData());

            image.beginDetection(0.5f);
            image.failDetection("Model error");

            assertThat(image.isFailed()).isTrue();
        }

        @Test
        @DisplayName("should track current task")
        void shouldTrackCurrentTask() {
            Image image = Image.create(createTestImageData());

            image.beginDetection(0.5f);

            assertThat(image.currentTask()).isEqualTo(VisionTask.DETECT);
        }
    }

    @Nested
    @DisplayName("captioning workflow")
    class CaptioningWorkflowTests {

        @Test
        @DisplayName("should complete captioning workflow")
        void shouldCompleteCaptioningWorkflow() {
            Image image = Image.create(createTestImageData());
            CaptionResult result = CaptionResult.of("A beautiful sunset");

            image.beginCaptioning();
            image.completeCaptioning(result);

            assertThat(image.isCompleted()).isTrue();
            assertThat(image.captionResult()).isPresent();
        }

        @Test
        @DisplayName("should fail captioning")
        void shouldFailCaptioning() {
            Image image = Image.create(createTestImageData());

            image.beginCaptioning();
            image.failCaptioning("Captioning failed");

            assertThat(image.isFailed()).isTrue();
        }
    }

    @Nested
    @DisplayName("OCR workflow")
    class OcrWorkflowTests {

        @Test
        @DisplayName("should complete OCR workflow")
        void shouldCompleteOcrWorkflow() {
            Image image = Image.create(createTestImageData());
            OcrResult result = OcrResult.of("Extracted text");

            image.beginOcr("en");
            image.completeOcr(result);

            assertThat(image.isCompleted()).isTrue();
            assertThat(image.ocrResult()).isPresent();
        }

        @Test
        @DisplayName("should fail OCR")
        void shouldFailOcr() {
            Image image = Image.create(createTestImageData());

            image.beginOcr("en");
            image.failOcr("OCR error");

            assertThat(image.isFailed()).isTrue();
        }
    }

    @Nested
    @DisplayName("generation workflow")
    class GenerationWorkflowTests {

        @Test
        @DisplayName("should complete generation workflow")
        void shouldCompleteGenerationWorkflow() {
            Image image = Image.create(createTestImageData());
            GeneratedImage result = GeneratedImage.fromUrl("https://example.com/image.png", 12345, null);

            image.beginGeneration(GenerateParams.fromPrompt("prompt"));
            image.completeGeneration(result);

            assertThat(image.isCompleted()).isTrue();
            assertThat(image.generatedImage()).isPresent();
        }

        @Test
        @DisplayName("should reject null params for generation")
        void shouldRejectNullParamsForGeneration() {
            Image image = Image.create(createTestImageData());

            assertThatThrownBy(() -> image.beginGeneration(null))
                    .isInstanceOf(IllegalArgumentException.class);
        }
    }

    @Nested
    @DisplayName("reset functionality")
    class ResetFunctionalityTests {

        @Test
        @DisplayName("should reset completed image to PENDING")
        void shouldResetCompletedImageToPending() {
            Image image = Image.create(createTestImageData());
            image.beginDetection(0.5f);
            image.completeDetection(DetectionResult.of(
                java.util.List.of(new DetectionResult.DetectedObject("test", 0.9f,
                    new DetectionResult.BoundingBox(0, 0, 100, 100)))
            ));

            image.reset();

            assertThat(image.isPending()).isTrue();
        }

        @Test
        @DisplayName("should reset failed image to PENDING")
        void shouldResetFailedImageToPending() {
            Image image = Image.create(createTestImageData());
            image.beginDetection(0.5f);
            image.failDetection("Error");

            image.reset();

            assertThat(image.isPending()).isTrue();
        }

        @Test
        @DisplayName("should not reset PENDING image")
        void shouldNotResetPendingImage() {
            Image image = Image.create(createTestImageData());

            assertThatThrownBy(() -> image.reset())
                    .isInstanceOf(IllegalStateException.class);
        }

        @Test
        @DisplayName("should add domain event on reset")
        void shouldAddDomainEventOnReset() {
            Image image = Image.create(createTestImageData());
            image.beginDetection(0.5f);
            image.completeDetection(DetectionResult.of(
                java.util.List.of(new DetectionResult.DetectedObject("test", 0.9f,
                    new DetectionResult.BoundingBox(0, 0, 100, 100)))
            ));

            image.reset();

            assertThat(image.domainEvents()).anyMatch(e -> e instanceof Image.ImageResetEvent);
        }
    }

    @Nested
    @DisplayName("domain events")
    class DomainEventTests {

        @Test
        @DisplayName("should emit state transition events")
        void shouldEmitStateTransitionEvents() {
            Image image = Image.create(createTestImageData());

            image.beginDetection(0.5f);

            assertThat(image.domainEvents()).anyMatch(e -> e instanceof Image.StateTransitionEvent);
        }
    }

    @Nested
    @DisplayName("State enum values")
    class StateEnumTests {

        @Test
        @DisplayName("should have all expected state values")
        void shouldHaveAllExpectedStateValues() {
            assertThat(Image.State.values()).containsExactlyInAnyOrder(
                    Image.State.PENDING,
                    Image.State.ANALYZING,
                    Image.State.COMPLETED,
                    Image.State.FAILED
            );
        }
    }
}
