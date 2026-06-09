package com.ai.agents.domain;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("PipelineRun Tests")
class PipelineRunTest {

    @Nested
    @DisplayName("start factory method")
    class StartFactoryMethodTests {

        @Test
        @DisplayName("should create pipeline with RUNNING status")
        void shouldCreatePipelineWithRunningStatus() {
            PipelineRun run = PipelineRun.start("test-pipeline", List.of("step1", "step2"));

            assertThat(run.status()).isEqualTo(PipelineRun.PipelineStatus.RUNNING);
            assertThat(run.pipelineName()).isEqualTo("test-pipeline");
        }

        @Test
        @DisplayName("should initialize with empty step results")
        void shouldInitializeWithEmptyStepResults() {
            PipelineRun run = PipelineRun.start("test", List.of());

            assertThat(run.stepResults()).isEmpty();
        }

        @Test
        @DisplayName("should set execution order")
        void shouldSetExecutionOrder() {
            List<String> steps = List.of("step1", "step2", "step3");
            PipelineRun run = PipelineRun.start("test", steps);

            assertThat(run.executionOrder()).containsExactlyElementsOf(steps);
        }

        @Test
        @DisplayName("should generate unique pipeline id")
        void shouldGenerateUniquePipelineId() {
            PipelineRun run1 = PipelineRun.start("test", List.of());
            PipelineRun run2 = PipelineRun.start("test", List.of());

            assertThat(run1.id()).isNotEqualTo(run2.id());
        }

        @Test
        @DisplayName("should handle null steps")
        void shouldHandleNullSteps() {
            PipelineRun run = PipelineRun.start("test", null);

            assertThat(run.executionOrder()).isEmpty();
        }

        @Test
        @DisplayName("should set startedAt timestamp")
        void shouldSetStartedAtTimestamp() {
            PipelineRun run = PipelineRun.start("test", List.of());

            assertThat(run.startedAt()).isNotNull();
        }
    }

    @Nested
    @DisplayName("recordStepStart method")
    class RecordStepStartMethodTests {

        @Test
        @DisplayName("should record step as running")
        void shouldRecordStepAsRunning() {
            PipelineRun run = PipelineRun.start("test", List.of("step1"));

            PipelineRun recorded = run.recordStepStart("step1");

            assertThat(recorded.stepResults().get("step1").status())
                    .isEqualTo(PipelineRun.StepStatus.RUNNING);
        }

        @Test
        @DisplayName("should preserve other step results")
        void shouldPreserveOtherStepResults() {
            PipelineRun run = PipelineRun.start("test", List.of("step1", "step2"))
                    .recordStepComplete("step1", "result1");

            PipelineRun recorded = run.recordStepStart("step2");

            assertThat(recorded.stepResults()).containsKey("step1");
            assertThat(recorded.stepResults()).containsKey("step2");
        }
    }

    @Nested
    @DisplayName("recordStepComplete method")
    class RecordStepCompleteMethodTests {

        @Test
        @DisplayName("should record step as successful")
        void shouldRecordStepAsSuccessful() {
            PipelineRun run = PipelineRun.start("test", List.of("step1"));

            PipelineRun recorded = run.recordStepComplete("step1", "output");

            assertThat(recorded.stepResults().get("step1").isSuccess()).isTrue();
        }

        @Test
        @DisplayName("should store step output")
        void shouldStoreStepOutput() {
            PipelineRun run = PipelineRun.start("test", List.of("step1"));
            Map<String, Object> output = Map.of("result", "success");

            PipelineRun recorded = run.recordStepComplete("step1", output);

            assertThat(recorded.stepResults().get("step1").output()).isEqualTo(output);
        }
    }

    @Nested
    @DisplayName("recordStepFailed method")
    class RecordStepFailedMethodTests {

        @Test
        @DisplayName("should record step as failed")
        void shouldRecordStepAsFailed() {
            PipelineRun run = PipelineRun.start("test", List.of("step1"));

            PipelineRun recorded = run.recordStepFailed("step1", "Error occurred");

            assertThat(recorded.stepResults().get("step1").isFailed()).isTrue();
        }

        @Test
        @DisplayName("should store error message")
        void shouldStoreErrorMessage() {
            PipelineRun run = PipelineRun.start("test", List.of("step1"));

            PipelineRun recorded = run.recordStepFailed("step1", "Connection timeout");

            assertThat(recorded.stepResults().get("step1").error()).isEqualTo("Connection timeout");
        }

        @Test
        @DisplayName("should set overall status to FAILED")
        void shouldSetOverallStatusToFailed() {
            PipelineRun run = PipelineRun.start("test", List.of("step1"));

            PipelineRun recorded = run.recordStepFailed("step1", "Error");

            assertThat(recorded.status()).isEqualTo(PipelineRun.PipelineStatus.FAILED);
        }
    }

    @Nested
    @DisplayName("complete method")
    class CompleteMethodTests {

        @Test
        @DisplayName("should set status to COMPLETED when all steps succeed")
        void shouldSetStatusToCompletedWhenAllStepsSucceed() {
            PipelineRun run = PipelineRun.start("test", List.of("step1", "step2"))
                    .recordStepComplete("step1", "result1")
                    .recordStepComplete("step2", "result2");

            PipelineRun completed = run.complete();

            assertThat(completed.status()).isEqualTo(PipelineRun.PipelineStatus.COMPLETED);
        }

        @Test
        @DisplayName("should set status to FAILED when any step fails")
        void shouldSetStatusToFailedWhenAnyStepFails() {
            PipelineRun run = PipelineRun.start("test", List.of("step1", "step2"))
                    .recordStepComplete("step1", "result1")
                    .recordStepFailed("step2", "Error");

            PipelineRun completed = run.complete();

            assertThat(completed.status()).isEqualTo(PipelineRun.PipelineStatus.FAILED);
        }

        @Test
        @DisplayName("should set completedAt timestamp")
        void shouldSetCompletedAtTimestamp() {
            PipelineRun run = PipelineRun.start("test", List.of("step1"))
                    .recordStepComplete("step1", "result");

            PipelineRun completed = run.complete();

            assertThat(completed.completedAt()).isNotNull();
        }
    }

    @Nested
    @DisplayName("cancel method")
    class CancelMethodTests {

        @Test
        @DisplayName("should set status to CANCELLED")
        void shouldSetStatusToCancelled() {
            PipelineRun run = PipelineRun.start("test", List.of("step1"));

            PipelineRun cancelled = run.cancel();

            assertThat(cancelled.status()).isEqualTo(PipelineRun.PipelineStatus.CANCELLED);
        }

        @Test
        @DisplayName("should preserve step results")
        void shouldPreserveStepResults() {
            PipelineRun run = PipelineRun.start("test", List.of("step1"))
                    .recordStepComplete("step1", "result");

            PipelineRun cancelled = run.cancel();

            assertThat(cancelled.stepResults()).containsKey("step1");
        }
    }

    @Nested
    @DisplayName("status checking methods")
    class StatusCheckingMethodTests {

        @Test
        @DisplayName("isRunning should return true for RUNNING status")
        void isRunningShouldReturnTrueForRunningStatus() {
            PipelineRun run = PipelineRun.start("test", List.of());

            assertThat(run.isRunning()).isTrue();
        }

        @Test
        @DisplayName("isCompleted should return true for COMPLETED status")
        void isCompletedShouldReturnTrueForCompletedStatus() {
            PipelineRun run = PipelineRun.start("test", List.of("step1"))
                    .recordStepComplete("step1", "result")
                    .complete();

            assertThat(run.isCompleted()).isTrue();
        }

        @Test
        @DisplayName("isFailed should return true for FAILED status")
        void isFailedShouldReturnTrueForFailedStatus() {
            PipelineRun run = PipelineRun.start("test", List.of("step1"))
                    .recordStepFailed("step1", "Error");

            assertThat(run.isFailed()).isTrue();
        }

        @Test
        @DisplayName("isStepCompleted should return true for completed step")
        void isStepCompletedShouldReturnTrueForCompletedStep() {
            PipelineRun run = PipelineRun.start("test", List.of("step1"))
                    .recordStepComplete("step1", "result");

            assertThat(run.isStepCompleted("step1")).isTrue();
        }

        @Test
        @DisplayName("isStepFailed should return true for failed step")
        void isStepFailedShouldReturnTrueForFailedStep() {
            PipelineRun run = PipelineRun.start("test", List.of("step1"))
                    .recordStepFailed("step1", "Error");

            assertThat(run.isStepFailed("step1")).isTrue();
        }
    }

    @Nested
    @DisplayName("getDurationSeconds method")
    class GetDurationSecondsMethodTests {

        @Test
        @DisplayName("should return duration in seconds")
        void shouldReturnDurationInSeconds() throws InterruptedException {
            PipelineRun run = PipelineRun.start("test", List.of("step1"));

            Thread.sleep(100);

            assertThat(run.getDurationSeconds()).isGreaterThanOrEqualTo(0);
        }

        @Test
        @DisplayName("should use completedAt when available")
        void shouldUseCompletedAtWhenAvailable() {
            PipelineRun run = PipelineRun.start("test", List.of("step1"))
                    .recordStepComplete("step1", "result")
                    .complete();

            long duration = run.getDurationSeconds();

            assertThat(duration).isGreaterThanOrEqualTo(0);
        }
    }

    @Nested
    @DisplayName("StepResult record")
    class StepResultTests {

        @Test
        @DisplayName("running factory should create RUNNING status")
        void runningFactoryShouldCreateRunningStatus() {
            PipelineRun.StepResult result = PipelineRun.StepResult.running();

            assertThat(result.status()).isEqualTo(PipelineRun.StepStatus.RUNNING);
        }

        @Test
        @DisplayName("success factory should create SUCCESS status")
        void successFactoryShouldCreateSuccessStatus() {
            PipelineRun.StepResult result = PipelineRun.StepResult.success("output");

            assertThat(result.isSuccess()).isTrue();
        }

        @Test
        @DisplayName("failed factory should create FAILED status")
        void failedFactoryShouldCreateFailedStatus() {
            PipelineRun.StepResult result = PipelineRun.StepResult.failed("error");

            assertThat(result.isFailed()).isTrue();
            assertThat(result.error()).isEqualTo("error");
        }
    }
}
