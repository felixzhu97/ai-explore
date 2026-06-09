package com.ai.rag.infrastructure.persistence;

import com.ai.rag.domain.*;

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * In-memory implementation of DocumentRepository.
 * Thread-safe using ConcurrentHashMap.
 */
public class InMemoryDocumentRepository implements DocumentRepository {

    private final Map<DocumentId, DocumentRecord> documents = new ConcurrentHashMap<>();

    @Override
    public Document save(Document document) {
        documents.put(document.getId(), new DocumentRecord(document));
        return document;
    }

    @Override
    public void update(Document document) {
        DocumentRecord existing = documents.get(document.getId());
        if (existing != null) {
            documents.put(document.getId(), new DocumentRecord(document));
        }
    }

    @Override
    public Optional<Document> findById(DocumentId id) {
        DocumentRecord record = documents.get(id);
        return record != null ? Optional.of(record.document) : Optional.empty();
    }

    @Override
    public List<Document> findAll(int page, int size) {
        return documents.values().stream()
                .sorted((a, b) -> b.document.getCreatedAt().compareTo(a.document.getCreatedAt()))
                .skip((long) page * size)
                .limit(size)
                .map(record -> record.document)
                .collect(Collectors.toList());
    }

    @Override
    public List<Document> findByStatus(DocumentStatus status, int page, int size) {
        return documents.values().stream()
                .filter(record -> record.document.getStatus() == status)
                .sorted((a, b) -> b.document.getCreatedAt().compareTo(a.document.getCreatedAt()))
                .skip((long) page * size)
                .limit(size)
                .map(record -> record.document)
                .collect(Collectors.toList());
    }

    @Override
    public void deleteById(DocumentId id) {
        documents.remove(id);
    }

    @Override
    public long count() {
        return documents.size();
    }

    @Override
    public long countByStatus(DocumentStatus status) {
        return documents.values().stream()
                .filter(record -> record.document.getStatus() == status)
                .count();
    }

    @Override
    public Set<DocumentId> findAllIds() {
        return new HashSet<>(documents.keySet());
    }

    @Override
    public boolean exists(DocumentId id) {
        return documents.containsKey(id);
    }

    /**
     * Internal record for storing document.
     */
    private record DocumentRecord(Document document) {}
}
