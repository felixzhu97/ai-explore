package com.ai.vision.domain;

/**
 * Enumeration of supported vision model types.
 */
public enum ModelType {
    YOLO,
    BLIP,
    OCR,
    STABLE_DIFFUSION;

    public static ModelType fromVisionTask(VisionTask task) {
        return switch (task) {
            case DETECT -> YOLO;
            case CAPTION -> BLIP;
            case OCR -> OCR;
            case GENERATE -> STABLE_DIFFUSION;
        };
    }
}
