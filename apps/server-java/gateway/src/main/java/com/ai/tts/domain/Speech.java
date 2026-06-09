package com.ai.tts.domain;

import com.ai.tts.domain.exception.TtsDomainException;

import java.util.Objects;

public final class Speech {

    private final SpeechId id;
    private final SynthesisRequest request;
    private AudioResult result;
    private SpeechStatus status;

    public enum SpeechStatus {
        PENDING,
        SYNTHESIZING,
        COMPLETED,
        FAILED
    }

    private Speech(SpeechId id, SynthesisRequest request) {
        this.id = Objects.requireNonNull(id, "SpeechId cannot be null");
        this.request = Objects.requireNonNull(request, "SynthesisRequest cannot be null");
        this.status = SpeechStatus.PENDING;
    }

    public static Speech create(SynthesisRequest request) {
        return new Speech(request.speechId(), request);
    }

    public static Speech fromRequest(String text, String voice, String language,
                                     float speed, float pitch, OutputFormat format, String provider) {
        SynthesisRequest request = SynthesisRequest.create(text, voice, language, speed, pitch, format, provider);
        return new Speech(request.speechId(), request);
    }

    public SpeechId id() {
        return id;
    }

    public SynthesisRequest request() {
        return request;
    }

    public AudioResult result() {
        return result;
    }

    public SpeechStatus status() {
        return status;
    }

    public boolean isCompleted() {
        return status == SpeechStatus.COMPLETED;
    }

    public boolean isFailed() {
        return status == SpeechStatus.FAILED;
    }

    public boolean canStream() {
        return request.text().length() > 500;
    }

    public String text() {
        return request.text();
    }

    public String voice() {
        return request.voice();
    }

    public String language() {
        return request.language();
    }

    public float speed() {
        return request.speed();
    }

    public float pitch() {
        return request.pitch();
    }

    public OutputFormat outputFormat() {
        return request.outputFormat();
    }

    public String provider() {
        return request.provider();
    }

    public void startSynthesis() {
        if (status != SpeechStatus.PENDING) {
            throw TtsDomainException.synthesisFailed(
                "Cannot start synthesis, current status: " + status, null
            );
        }
        this.status = SpeechStatus.SYNTHESIZING;
    }

    public void complete(byte[] audioData) {
        if (status != SpeechStatus.SYNTHESIZING) {
            throw TtsDomainException.synthesisFailed(
                "Cannot complete synthesis, current status: " + status, null
            );
        }
        this.result = AudioResult.of(id, audioData, request.outputFormat());
        this.status = SpeechStatus.COMPLETED;
    }

    public void fail(Throwable cause) {
        this.status = SpeechStatus.FAILED;
        throw TtsDomainException.synthesisFailed(cause.getMessage(), cause);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Speech speech = (Speech) o;
        return Objects.equals(id, speech.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
