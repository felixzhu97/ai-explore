package com.ai.domain.model;

public record SourceDocument(
    int index,
    String text,
    double score,
    String documentTitle,
    java.util.Map<String, Object> metadata
) {}
