package com.ai.vision.domain;

/**
 * Enumeration of supported vision task types.
 */
public enum VisionTask {
    DETECT,
    CAPTION,
    OCR,
    GENERATE;

    public static VisionTask fromString(String task) {
        if (task == null) {
            return DETECT;
        }
        return switch (task.toLowerCase().trim()) {
            case "detect", "detection", "detect_objects" -> DETECT;
            case "caption", "captioning", "describe" -> CAPTION;
            case "ocr", "recognize", "read_text" -> OCR;
            case "generate", "generation", "create_image" -> GENERATE;
            default -> throw new IllegalArgumentException("Unknown vision task: " + task);
        };
    }

    public boolean isValidTransition(VisionTask next) {
        return switch (this) {
            case DETECT, CAPTION, OCR -> next == GENERATE;
            case GENERATE -> false;
        };
    }
}
