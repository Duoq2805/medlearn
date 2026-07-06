package com.duoq.medlearn.document.repository;

import com.duoq.medlearn.document.entity.Document;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface DocumentRepository extends JpaRepository<Document, Long> {

    Page<Document> findAllByCreatedByIdAndDeletedAtIsNull(Long userId, Pageable pageable);

    Page<Document> findAllByDeletedAtIsNull(Pageable pageable);

    Optional<Document> findByIdAndDeletedAtIsNull(Long id);

    List<Document> findAllByDeletedAtIsNull();
}
