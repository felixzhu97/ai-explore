package com.ai.application.service;

import com.ai.application.port.DocumentRepositoryPort;
import com.ai.application.usecase.*;
import com.ai.domain.model.Document;
import com.ai.application.usecase.RagChatUseCase.RetrievalResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

public class RagApplicationService {
    private static final Logger log = LoggerFactory.getLogger(RagApplicationService.class);

    private final UploadDocumentUseCase uploadDocumentUseCase;
    private final DeleteDocumentUseCase deleteDocumentUseCase;
    private final RagChatUseCase ragChatUseCase;
    private final DocumentRepositoryPort documentRepository;

    public RagApplicationService(
            UploadDocumentUseCase uploadDocumentUseCase,
            DeleteDocumentUseCase deleteDocumentUseCase,
            RagChatUseCase ragChatUseCase,
            DocumentRepositoryPort documentRepository) {
        this.uploadDocumentUseCase = uploadDocumentUseCase;
        this.deleteDocumentUseCase = deleteDocumentUseCase;
        this.ragChatUseCase = ragChatUseCase;
        this.documentRepository = documentRepository;
    }

    public Document uploadDocument(String title, String fileName, Long fileSize, String content) {
        return uploadDocumentUseCase.execute(title, fileName, fileSize, content);
    }

    public List<Document> listDocuments() {
        return documentRepository.findAll();
    }

    public void deleteDocument(UUID documentId) {
        deleteDocumentUseCase.execute(documentId);
    }

    public RetrievalResult retrieveContext(String query, List<UUID> docIds, int topK) {
        return ragChatUseCase.execute(query, docIds, topK);
    }
}
