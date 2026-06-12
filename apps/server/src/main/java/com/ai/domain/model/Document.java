package com.ai.domain.model;

import com.ai.domain.vo.DocumentId;

import java.time.Instant;

public class Document {
    private final DocumentId id;
    private String title;
    private String fileName;
    private Long fileSize;
    private DocumentStatus status;
    private final Instant createdAt;
    private Instant updatedAt;

    public Document(DocumentId id, String title, String fileName, Long fileSize) {
        this.id = id;
        this.title = title;
        this.fileName = fileName;
        this.fileSize = fileSize;
        this.status = DocumentStatus.UPLOADING;
        this.createdAt = Instant.now();
        this.updatedAt = Instant.now();
    }

    // Full constructor for repository mapper to restore all fields
    public Document(DocumentId id, String title, String fileName, Long fileSize,
                   DocumentStatus status, Instant createdAt, Instant updatedAt) {
        this.id = id;
        this.title = title;
        this.fileName = fileName;
        this.fileSize = fileSize;
        this.status = status;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public void markProcessing() { this.status = DocumentStatus.PROCESSING; this.updatedAt = Instant.now(); }
    public void markReady() { this.status = DocumentStatus.READY; this.updatedAt = Instant.now(); }
    public void markFailed() { this.status = DocumentStatus.FAILED; this.updatedAt = Instant.now(); }
    public boolean isReady() { return this.status == DocumentStatus.READY; }
    
    // Getters only, no setters - use business methods
    public DocumentId getId() { return id; }
    public String getTitle() { return title; }
    public String getFileName() { return fileName; }
    public Long getFileSize() { return fileSize; }
    public DocumentStatus getStatus() { return status; }
    public Instant getCreatedAt() { return createdAt; }
    public Instant getUpdatedAt() { return updatedAt; }
}
