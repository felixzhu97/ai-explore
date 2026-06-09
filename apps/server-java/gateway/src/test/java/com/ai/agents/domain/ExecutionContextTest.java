package com.ai.agents.domain;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("ExecutionContext Tests")
class ExecutionContextTest {

    @Nested
    @DisplayName("start factory methods")
    class StartFactoryMethodTests {

        @Test
        @DisplayName("should create context with operation name")
        void shouldCreateContextWithOperationName() {
            ExecutionContext context = ExecutionContext.start("test-operation");

            assertThat(context.operationName()).isEqualTo("test-operation");
            assertThat(context.status()).isEqualTo(ExecutionContext.ExecutionStatus.RUNNING);
        }

        @Test
        @DisplayName("should create context with empty parameters")
        void shouldCreateContextWithEmptyParameters() {
            ExecutionContext context = ExecutionContext.start("test");

            assertThat(context.parameters()).isEmpty();
        }

        @Test
        @DisplayName("should create context with initial parameters")
        void shouldCreateContextWithInitialParameters() {
            Map<String, Object> params = Map.of("key", "value");

            ExecutionContext context = ExecutionContext.start("test", params);

            assertThat(context.getParameter("key")).isEqualTo("value");
        }

        @Test
        @DisplayName("should set default max tool calls to 10")
        void shouldSetDefaultMaxToolCallsTo10() {
            ExecutionContext context = ExecutionContext.start("test");

            assertThat(context.maxToolCalls()).isEqualTo(10);
        }

        @Test
        @DisplayName("should generate unique execution id")
        void shouldGenerateUniqueExecutionId() {
            ExecutionContext context1 = ExecutionContext.start("test");
            ExecutionContext context2 = ExecutionContext.start("test");

            assertThat(context1.id()).isNotEqualTo(context2.id());
        }
    }

    @Nested
    @DisplayName("addToolCall method")
    class AddToolCallMethodTests {

        @Test
        @DisplayName("should add tool call to context")
        void shouldAddToolCallToContext() {
            ExecutionContext context = ExecutionContext.start("test");
            ToolCall toolCall = ToolCall.create("getWeather", Map.of("city", "Beijing"));

            ExecutionContext updated = context.addToolCall(toolCall);

            assertThat(updated.toolCalls()).hasSize(1);
            assertThat(updated.toolCallCount()).isEqualTo(1);
        }

        @Test
        @DisplayName("should increment tool call count")
        void shouldIncrementToolCallCount() {
            ExecutionContext context = ExecutionContext.start("test");
            ToolCall toolCall = ToolCall.create("tool1", Map.of());

            ExecutionContext updated = context.addToolCall(toolCall);

            assertThat(updated.toolCallCount()).isGreaterThan(context.toolCallCount());
        }

        @Test
        @DisplayName("should throw exception when exceeding max tool calls")
        void shouldThrowExceptionWhenExceedingMaxToolCalls() {
            ExecutionContext context = ExecutionContext.start("test").withMaxToolCalls(2);
            ToolCall tool1 = ToolCall.create("tool1", Map.of());
            ToolCall tool2 = ToolCall.create("tool2", Map.of());
            context = context.addToolCall(tool1).addToolCall(tool2);

            assertThat(context.hasExceededToolCallLimit()).isTrue();
        }
    }

    @Nested
    @DisplayName("addResult methods")
    class AddResultMethodTests {

        @Test
        @DisplayName("should add single result")
        void shouldAddSingleResult() {
            ExecutionContext context = ExecutionContext.start("test");

            ExecutionContext updated = context.addResult("key", "value");

            assertThat(updated.getResult("key")).isEqualTo("value");
        }

        @Test
        @DisplayName("should add multiple results")
        void shouldAddMultipleResults() {
            ExecutionContext context = ExecutionContext.start("test");
            Map<String, Object> results = Map.of("a", 1, "b", 2);

            ExecutionContext updated = context.addResults(results);

            assertThat(updated.getResult("a")).isEqualTo(1);
            assertThat(updated.getResult("b")).isEqualTo(2);
        }

        @Test
        @DisplayName("should preserve existing results when adding more")
        void shouldPreserveExistingResultsWhenAddingMore() {
            ExecutionContext context = ExecutionContext.start("test").addResult("existing", "data");
            Map<String, Object> newResults = Map.of("new", "data");

            ExecutionContext updated = context.addResults(newResults);

            assertThat(updated.getResult("existing")).isEqualTo("data");
            assertThat(updated.getResult("new")).isEqualTo("data");
        }

        @Test
        @DisplayName("should return unmodifiable results map")
        void shouldReturnUnmodifiableResultsMap() {
            ExecutionContext context = ExecutionContext.start("test");

            assertThat(context.results()).isUnmodifiable();
        }
    }

    @Nested
    @DisplayName("complete method")
    class CompleteMethodTests {

        @Test
        @DisplayName("should set status to COMPLETED")
        void shouldSetStatusToCompleted() {
            ExecutionContext context = ExecutionContext.start("test");

            ExecutionContext completed = context.complete();

            assertThat(completed.status()).isEqualTo(ExecutionContext.ExecutionStatus.COMPLETED);
            assertThat(completed.isCompleted()).isTrue();
        }

        @Test
        @DisplayName("should preserve tool calls when completing")
        void shouldPreserveToolCallsWhenCompleting() {
            ExecutionContext context = ExecutionContext.start("test");
            context = context.addToolCall(ToolCall.create("tool1", Map.of()));

            ExecutionContext completed = context.complete();

            assertThat(completed.toolCalls()).hasSize(1);
        }
    }

    @Nested
    @DisplayName("fail method")
    class FailMethodTests {

        @Test
        @DisplayName("should set status to FAILED")
        void shouldSetStatusToFailed() {
            ExecutionContext context = ExecutionContext.start("test");

            ExecutionContext failed = context.fail("Error occurred");

            assertThat(failed.status()).isEqualTo(ExecutionContext.ExecutionStatus.FAILED);
            assertThat(failed.isFailed()).isTrue();
        }

        @Test
        @DisplayName("should add error to results")
        void shouldAddErrorToResults() {
            ExecutionContext context = ExecutionContext.start("test");

            ExecutionContext failed = context.fail("Error occurred");

            assertThat(failed.getResult("_error")).isEqualTo("Error occurred");
        }
    }

    @Nested
    @DisplayName("withMaxToolCalls method")
    class WithMaxToolCallsMethodTests {

        @Test
        @DisplayName("should change max tool calls limit")
        void shouldChangeMaxToolCallsLimit() {
            ExecutionContext context = ExecutionContext.start("test");

            ExecutionContext updated = context.withMaxToolCalls(5);

            assertThat(updated.maxToolCalls()).isEqualTo(5);
        }

        @Test
        @DisplayName("should preserve other context properties")
        void shouldPreserveOtherContextProperties() {
            ExecutionContext context = ExecutionContext.start("test").addResult("key", "value");

            ExecutionContext updated = context.withMaxToolCalls(5);

            assertThat(updated.getResult("key")).isEqualTo("value");
            assertThat(updated.status()).isEqualTo(ExecutionContext.ExecutionStatus.RUNNING);
        }
    }

    @Nested
    @DisplayName("status checking methods")
    class StatusCheckingMethodTests {

        @Test
        @DisplayName("isRunning should return true for RUNNING status")
        void isRunningShouldReturnTrueForRunningStatus() {
            ExecutionContext context = ExecutionContext.start("test");

            assertThat(context.isRunning()).isTrue();
        }

        @Test
        @DisplayName("isCompleted should return true for COMPLETED status")
        void isCompletedShouldReturnTrueForCompletedStatus() {
            ExecutionContext context = ExecutionContext.start("test").complete();

            assertThat(context.isCompleted()).isTrue();
        }

        @Test
        @DisplayName("isFailed should return true for FAILED status")
        void isFailedShouldReturnTrueForFailedStatus() {
            ExecutionContext context = ExecutionContext.start("test").fail("error");

            assertThat(context.isFailed()).isTrue();
        }
    }

    @Nested
    @DisplayName("getElapsedMillis method")
    class GetElapsedMillisMethodTests {

        @Test
        @DisplayName("should return elapsed time in milliseconds")
        void shouldReturnElapsedTimeInMilliseconds() throws InterruptedException {
            ExecutionContext context = ExecutionContext.start("test");

            Thread.sleep(50);

            assertThat(context.getElapsedMillis()).isGreaterThanOrEqualTo(50);
        }
    }
}
