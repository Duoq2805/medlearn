package com.duoq.medlearn.draft.repository;

import com.duoq.medlearn.draft.entity.DiseaseDraft;
import com.duoq.medlearn.draft.enums.DraftStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface DiseaseDraftRepository extends JpaRepository<DiseaseDraft, Long> {

    Page<DiseaseDraft> findAllByCreatedByIdAndDeletedAtIsNull(Long userId, Pageable pageable);

    Page<DiseaseDraft> findAllByDiseaseIdAndDeletedAtIsNull(Long diseaseId, Pageable pageable);

    Page<DiseaseDraft> findAllByStatusAndDeletedAtIsNull(DraftStatus status, Pageable pageable);

    Optional<DiseaseDraft> findByIdAndDeletedAtIsNull(Long id);
}
