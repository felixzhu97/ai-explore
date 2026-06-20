package com.ai.modules.rag.domain.repository;

import com.ai.modules.rag.infrastructure.storage.DocumentEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

/**
 * Spring Data JPA repository for DocumentEntity.
 */
@Repository
public interface ISpringDataDocumentRepository extends JpaRepository<DocumentEntity, UUID> {
}
