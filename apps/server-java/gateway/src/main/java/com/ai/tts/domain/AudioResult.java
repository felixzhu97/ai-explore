package com.ai.tts.domain;

public record AudioResult(
    SpeechId speechId,
    byte[] audioData,
    OutputFormat format,
    long timestamp
) {
    public static AudioResult of(SpeechId speechId, byte[] audioData, OutputFormat format) {
        return new AudioResult(speechId, audioData, format, System.currentTimeMillis());
    }

    public int sizeInBytes() {
        return audioData != null ? audioData.length : 0;
    }

    public String mediaType() {
        return format.mediaType();
    }

    public String extension() {
        return format.extension();
    }
}
