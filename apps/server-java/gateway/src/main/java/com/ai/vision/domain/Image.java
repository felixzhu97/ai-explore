package com.ai.vision.domain;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Aggregate Root for Image analysis domain.
 * 
 * Rich domain model with:
 * - Private constructor (factory method only)
 * - State machine: PENDING → ANALYZING → COMPLETED / FAILED
 * - State transition validation
 * - Business methods delegating to VisionModel port
 * - Domain events for state changes
 * 
 * This is the CORE of the DDD approach - all business logic for image
 * processing lives here, completely decoupled from Spring/infrastructure.
 */
public class Image {

    // State machine states
    public enum State {
        PENDING,
        ANALYZING,
        COMPLETED,
        FAILED
    }

    private final ImageId id;
    private final ImageData data;
    private State state;
    private Instant createdAt;
    private Instant updatedAt;

    // Analysis results
    private DetectionResult detectionResult;
    private CaptionResult captionResult;
    private OcrResult ocrResult;
    private GeneratedImage generatedImage;

    // Current task being performed
    private VisionTask currentTask;

    // Domain events (for event sourcing if needed)
    private final List<Object> domainEvents;

    private Image(ImageId id, ImageData data) {
        this.id = id;
        this.data = data;
        this.state = State.PENDING;
        this.createdAt = Instant.now();
        this.updatedAt = Instant.now();
        this.domainEvents = new ArrayList<>();
    }

    /**
     * Factory method to create a new Image aggregate.
     * 
     * @param imageData The immutable image data
     * @return New Image aggregate in PENDING state
     */
    public static Image create(ImageData imageData) {
        if (imageData == null || imageData.isEmpty()) {
            throw new IllegalArgumentException("Image data cannot be null or empty");
        }
        return new Image(ImageId.generate(), imageData);
    }

    /**
     * Reconstitute Image from storage (e.g., database).
     */
    public static Image reconstitute(
            ImageId id,
            ImageData data,
            State state,
            Instant createdAt,
            Instant updatedAt) {
        Image image = new Image(id, data);
        image.state = state;
        image.createdAt = createdAt;
        image.updatedAt = updatedAt;
        return image;
    }

    // ============================================================
    // Getters (read-only access to domain data)
    // ============================================================

    public ImageId id() {
        return id;
    }

    public ImageData data() {
        return data;
    }

    public State state() {
        return state;
    }

    public Instant createdAt() {
        return createdAt;
    }

    public Instant updatedAt() {
        return updatedAt;
    }

    public VisionTask currentTask() {
        return currentTask;
    }

    public Optional<DetectionResult> detectionResult() {
        return Optional.ofNullable(detectionResult);
    }

    public Optional<CaptionResult> captionResult() {
        return Optional.ofNullable(captionResult);
    }

    public Optional<OcrResult> ocrResult() {
        return Optional.ofNullable(ocrResult);
    }

    public Optional<GeneratedImage> generatedImage() {
        return Optional.ofNullable(generatedImage);
    }

    public List<Object> domainEvents() {
        return List.copyOf(domainEvents);
    }

    // ============================================================
    // State queries
    // ============================================================

    public boolean isPending() {
        return state == State.PENDING;
    }

    public boolean isAnalyzing() {
        return state == State.ANALYZING;
    }

    public boolean isCompleted() {
        return state == State.COMPLETED;
    }

    public boolean isFailed() {
        return state == State.FAILED;
    }

    public boolean canTransitionTo(State newState) {
        if (state == State.FAILED) {
            return false; // Terminal state
        }
        if (state == State.COMPLETED) {
            return newState == State.ANALYZING; // Can re-analyze
        }
        return switch (state) {
            case PENDING -> newState == State.ANALYZING;
            case ANALYZING -> newState == State.COMPLETED || newState == State.FAILED;
            default -> false;
        };
    }

    // ============================================================
    // Business methods (state machine transitions)
    // ============================================================

    /**
     * Begin object detection analysis.
     * 
     * @param confidence Confidence threshold (0.0-1.0)
     * @throws IllegalStateException if state transition is invalid
     */
    public void beginDetection(float confidence) {
        validateAndTransition(State.ANALYZING);
        this.currentTask = VisionTask.DETECT;
    }

    /**
     * Complete detection with results.
     */
    public void completeDetection(DetectionResult result) {
        validateCurrentTask(VisionTask.DETECT);
        this.detectionResult = result;
        transitionTo(State.COMPLETED);
    }

    /**
     * Fail detection with error message.
     */
    public void failDetection(String errorMessage) {
        validateCurrentTask(VisionTask.DETECT);
        transitionTo(State.FAILED);
    }

    /**
     * Begin image captioning.
     */
    public void beginCaptioning() {
        validateAndTransition(State.ANALYZING);
        this.currentTask = VisionTask.CAPTION;
    }

    /**
     * Complete captioning with results.
     */
    public void completeCaptioning(CaptionResult result) {
        validateCurrentTask(VisionTask.CAPTION);
        this.captionResult = result;
        transitionTo(State.COMPLETED);
    }

    /**
     * Fail captioning with error message.
     */
    public void failCaptioning(String errorMessage) {
        validateCurrentTask(VisionTask.CAPTION);
        transitionTo(State.FAILED);
    }

    /**
     * Begin OCR text recognition.
     * 
     * @param language Language code for OCR
     */
    public void beginOcr(String language) {
        validateAndTransition(State.ANALYZING);
        this.currentTask = VisionTask.OCR;
    }

    /**
     * Complete OCR with results.
     */
    public void completeOcr(OcrResult result) {
        validateCurrentTask(VisionTask.OCR);
        this.ocrResult = result;
        transitionTo(State.COMPLETED);
    }

    /**
     * Fail OCR with error message.
     */
    public void failOcr(String errorMessage) {
        validateCurrentTask(VisionTask.OCR);
        transitionTo(State.FAILED);
    }

    /**
     * Begin image generation.
     * 
     * @param params Generation parameters
     */
    public void beginGeneration(GenerateParams params) {
        if (params == null) {
            throw new IllegalArgumentException("Generation params cannot be null");
        }
        validateAndTransition(State.ANALYZING);
        this.currentTask = VisionTask.GENERATE;
    }

    /**
     * Complete generation with results.
     */
    public void completeGeneration(GeneratedImage result) {
        validateCurrentTask(VisionTask.GENERATE);
        this.generatedImage = result;
        transitionTo(State.COMPLETED);
    }

    /**
     * Fail generation with error message.
     */
    public void failGeneration(String errorMessage) {
        validateCurrentTask(VisionTask.GENERATE);
        transitionTo(State.FAILED);
    }

    /**
     * Reset image to PENDING state for re-analysis.
     * Only allowed from COMPLETED or FAILED state.
     */
    public void reset() {
        if (state != State.COMPLETED && state != State.FAILED) {
            throw new IllegalStateException(
                "Cannot reset image from state %s. Only COMPLETED or FAILED states can be reset".formatted(state));
        }
        this.state = State.PENDING;
        this.currentTask = null;
        this.updatedAt = Instant.now();
        domainEvents.add(new ImageResetEvent(id));
    }

    // ============================================================
    // Private helpers
    // ============================================================

    private void validateAndTransition(State newState) {
        if (!canTransitionTo(newState)) {
            throw new IllegalStateException(
                "Invalid state transition from %s to %s".formatted(state, newState));
        }
        this.state = newState;
        this.updatedAt = Instant.now();
        domainEvents.add(new StateTransitionEvent(id, state, newState));
    }

    private void transitionTo(State newState) {
        this.state = newState;
        this.updatedAt = Instant.now();
        domainEvents.add(new StateTransitionEvent(id, state, newState));
    }

    private void validateCurrentTask(VisionTask expectedTask) {
        if (currentTask != expectedTask) {
            throw new IllegalStateException(
                "Current task is %s, but expected %s".formatted(currentTask, expectedTask));
        }
        if (state != State.ANALYZING) {
            throw new IllegalStateException(
                "Cannot complete task when state is %s (expected ANALYZING)".formatted(state));
        }
    }

    // ============================================================
    // Domain Events
    // ============================================================

    public record StateTransitionEvent(ImageId id, State fromState, State toState) {}

    public record ImageResetEvent(ImageId id) {}
}
