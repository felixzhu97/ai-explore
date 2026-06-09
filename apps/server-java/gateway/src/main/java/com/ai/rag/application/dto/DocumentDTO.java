package com.ai.rag.application.dto;

import com.ai.rag.domain.*;

import java.time.LocalDateTime;

/**
 * Data Transfer Object for Document.
 * Used for API responses and transfers.
 */
public record DocumentDTO(
        String id,
        String title,
        String contentType,
        Long size,
        int chunkCount,
        String status,
        LocalDateTime createdAt
) {

    /**
     * Factory method to create DTO from domain Document.
     */
    public static DocumentDTO fromDomain(Document document) {
        return new DocumentDTO(
                document.getId().toString(),
                document.getTitle().value(),
                document.getContentType(),
                document.getSize(),
                document.getChunkCount(),
                document.getStatus().name(),
                document.getCreatedAt()
        );
    }
}
