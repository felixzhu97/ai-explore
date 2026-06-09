package com.ai.vision.presentation.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Map;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record VideoStatusResponse(
    @JsonProperty("task_id") String taskId,
    String status,
    @JsonProperty("video_url") String videoUrl,
    @JsonProperty("thumbnail_url") String thumbnailUrl,
    String error,
    @JsonProperty("processing_time_seconds") Double processingTimeSeconds,
    Map<String, Object> metadata
) {}
