package com.ai.vision.domain.exception;

/**
 * Domain exception for vision-related errors.
 * 
 * This exception is used throughout the domain layer to indicate
 * business rule violations and operational errors.
 */
public class VisionException extends RuntimeException {

    private final ErrorCode errorCode;
    private final String details;

    public enum ErrorCode {
        MODEL_NOT_AVAILABLE("Vision model is not available"),
        INVALID_IMAGE_DATA("Invalid or corrupted image data"),
        DETECTION_FAILED("Object detection failed"),
        CAPTION_FAILED("Image captioning failed"),
        OCR_FAILED("Text recognition failed"),
        GENERATION_FAILED("Image generation failed"),
        STATE_TRANSITION_INVALID("Invalid state transition for image"),
        TASK_NOT_SUPPORTED("Vision task not supported by this model"),
        UNSUPPORTED_OPERATION("Operation not yet implemented");

        private final String defaultMessage;

        ErrorCode(String defaultMessage) {
            this.defaultMessage = defaultMessage;
        }

        public String defaultMessage() {
            return defaultMessage;
        }
    }

    public VisionException(ErrorCode errorCode) {
        super(errorCode.defaultMessage());
        this.errorCode = errorCode;
        this.details = null;
    }

    public VisionException(ErrorCode errorCode, String message) {
        super(message);
        this.errorCode = errorCode;
        this.details = null;
    }

    public VisionException(ErrorCode errorCode, String message, Throwable cause) {
        super(message, cause);
        this.errorCode = errorCode;
        this.details = null;
    }

    public VisionException(ErrorCode errorCode, Throwable cause) {
        super(errorCode.defaultMessage(), cause);
        this.errorCode = errorCode;
        this.details = null;
    }

    public VisionException(ErrorCode errorCode, String message, String details) {
        super(message);
        this.errorCode = errorCode;
        this.details = details;
    }

    public ErrorCode errorCode() {
        return errorCode;
    }

    public String details() {
        return details;
    }

    public static VisionException modelNotAvailable(String modelName) {
        return new VisionException(ErrorCode.MODEL_NOT_AVAILABLE,
            "Vision model '%s' is not available".formatted(modelName));
    }

    public static VisionException invalidImageData() {
        return new VisionException(ErrorCode.INVALID_IMAGE_DATA,
            "Image data is invalid or corrupted");
    }

    public static VisionException detectionFailed(Throwable cause) {
        return new VisionException(ErrorCode.DETECTION_FAILED,
            "Object detection failed", cause);
    }

    public static VisionException captionFailed(Throwable cause) {
        return new VisionException(ErrorCode.CAPTION_FAILED,
            "Image captioning failed", cause);
    }

    public static VisionException ocrFailed(Throwable cause) {
        return new VisionException(ErrorCode.OCR_FAILED,
            "Text recognition failed", cause);
    }

    public static VisionException generationFailed(Throwable cause) {
        return new VisionException(ErrorCode.GENERATION_FAILED,
            "Image generation failed", cause);
    }

    public static VisionException stateTransitionInvalid(String fromState, String toState) {
        return new VisionException(ErrorCode.STATE_TRANSITION_INVALID,
            "Invalid state transition from %s to %s".formatted(fromState, toState));
    }
}
