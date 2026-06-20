package com.ai.modules.rag.domain.exception;

import java.util.UUID;

public class DocumentNotFoundException extends RuntimeException {
    public DocumentNotFoundException(UUID id) {
        super("Document not found: " + id);
    }
}
