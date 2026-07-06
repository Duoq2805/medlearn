package com.duoq.medlearn.document.repository;

import com.duoq.medlearn.document.entity.DocumentChunk;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DocumentChunkRepository extends JpaRepository<DocumentChunk, Long> {

    List<DocumentChunk> findAllByDocumentIdAndDeletedAtIsNullOrderByChunkIndex(Long documentId);

    void deleteAllByDocumentId(Long documentId);
}
