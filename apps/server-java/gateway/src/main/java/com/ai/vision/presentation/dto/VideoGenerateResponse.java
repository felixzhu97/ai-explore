package com.ai.vision.presentation.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record VideoGenerateResponse(
    @JsonProperty("task_id") String taskId,
    String status,
    String message,
    @JsonProperty("created_at") String createdAt
) {}
