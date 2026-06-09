package com.ai.media.domain;

import com.ai.media.domain.exception.MediaException;

import java.time.Instant;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

/**
 * Aggregate root representing an image generation task.
 * Implements rich domain model (anemic-free) with encapsulated state transitions.
 * 
 * State Machine:
 *   PENDING → GENERATING → COMPLETED
 *                    ↓
 *                  FAILED
 */
public final class GenerationTask {

    public enum Status {
        PENDING,
        GENERATING,
        COMPLETED,
        FAILED
    }

    private GenerationId id;
    private final GenerationParams params;
    private Instant createdAt;
    private Instant startedAt;
    private Instant completedAt;
    
    private Status status;
    private List<GeneratedImage> images;
    private String failureReason;

    private GenerationTask(GenerationParams params) {
        this.id = GenerationId.generate();
        this.params = Objects.requireNonNull(params, "Params cannot be null");
        this.createdAt = Instant.now();
        this.status = Status.PENDING;
    }

    /**
     * Factory method to create a new generation task.
     */
    public static GenerationTask create(GenerationParams params) {
        if (params == null) {
            throw new MediaException.InvalidParamsException("Generation parameters are required");
        }
        return new GenerationTask(params);
    }

    /**
     * Reconstruct a task from persistence (for event sourcing).
     */
    public static GenerationTask reconstitute(
            GenerationId id,
            GenerationParams params,
            Instant createdAt,
            Status status,
            List<GeneratedImage> images,
            String failureReason) {
        GenerationTask task = new GenerationTask(params);
        task.id = id;
        task.createdAt = createdAt;
        task.status = status;
        task.images = images;
        task.failureReason = failureReason;
        return task;
    }

    // Business methods

    /**
     * Check if the task can be executed.
     */
    public boolean canGenerate() {
        return status == Status.PENDING;
    }

    /**
     * Transition to GENERATING state.
     */
    public void startGeneration() {
        if (status != Status.PENDING) {
            throw new MediaException.GenerationException(
                    "Cannot start generation: task is in " + status + " state");
        }
        this.status = Status.GENERATING;
        this.startedAt = Instant.now();
    }

    /**
     * Execute generation using the provided provider.
     * Returns the generated image wrapped in Mono.
     */
    public reactor.core.publisher.Mono<GeneratedImage> generate(ImageProvider provider) {
        if (provider == null) {
            throw new MediaException.InvalidParamsException("ImageProvider is required");
        }
        
        if (!canGenerate()) {
            return reactor.core.publisher.Mono.error(
                    new MediaException.GenerationException("Cannot generate: task is in " + status + " state"));
        }

        startGeneration();
        
        return provider.generate(params)
                .flatMap(image -> {
                    markCompleted(List.of(image));
                    return reactor.core.publisher.Mono.just(image);
                })
                .onErrorResume(error -> {
                    markFailed(error.getMessage());
                    return reactor.core.publisher.Mono.error(error);
                });
    }

    /**
     * Mark task as completed with generated images.
     */
    public void markCompleted(List<GeneratedImage> images) {
        if (images == null || images.isEmpty()) {
            throw new MediaException.InvalidParamsException("At least one image is required");
        }
        this.status = Status.COMPLETED;
        this.images = List.copyOf(images);
        this.completedAt = Instant.now();
    }

    /**
     * Mark task as failed with reason.
     */
    public void markFailed(String reason) {
        if (reason == null || reason.isBlank()) {
            throw new MediaException.InvalidParamsException("Failure reason is required");
        }
        this.status = Status.FAILED;
        this.failureReason = reason;
        this.completedAt = Instant.now();
    }

    // Getters

    public GenerationId id() {
        return id;
    }

    public GenerationParams params() {
        return params;
    }

    public Status status() {
        return status;
    }

    public Instant createdAt() {
        return createdAt;
    }

    public Instant startedAt() {
        return startedAt;
    }

    public Instant completedAt() {
        return completedAt;
    }

    public List<GeneratedImage> images() {
        return images != null ? List.copyOf(images) : List.of();
    }

    public String failureReason() {
        return failureReason;
    }

    public long processingTimeMs() {
        if (startedAt == null) return 0;
        Instant end = completedAt != null ? completedAt : Instant.now();
        return end.toEpochMilli() - startedAt.toEpochMilli();
    }

    public boolean isCompleted() {
        return status == Status.COMPLETED;
    }

    public boolean isFailed() {
        return status == Status.FAILED;
    }

    public boolean isPending() {
        return status == Status.PENDING;
    }

    public boolean isGenerating() {
        return status == Status.GENERATING;
    }
}
