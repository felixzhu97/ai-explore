package com.ai.adapter.in.dto;

import java.util.List;

/**
 * Document list response DTO.
 */
public record DocumentListResponse(
    List<DocumentSummaryDto> documents
) {}
