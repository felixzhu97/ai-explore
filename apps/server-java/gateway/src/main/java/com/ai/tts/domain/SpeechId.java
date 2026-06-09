package com.ai.tts.domain;

public record SpeechId(String value) {

    public static SpeechId generate() {
        return new SpeechId(java.util.UUID.randomUUID().toString());
    }

    public static SpeechId of(String value) {
        if (value == null || value.isBlank()) {
            return generate();
        }
        return new SpeechId(value);
    }
}
