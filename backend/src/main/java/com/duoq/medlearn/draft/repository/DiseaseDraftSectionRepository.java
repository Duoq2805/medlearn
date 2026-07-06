package com.duoq.medlearn.draft.repository;

import com.duoq.medlearn.draft.entity.DiseaseDraftSection;
import com.duoq.medlearn.draft.enums.DraftSectionType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface DiseaseDraftSectionRepository extends JpaRepository<DiseaseDraftSection, Long> {

    List<DiseaseDraftSection> findAllByDraftIdAndDeletedAtIsNullOrderByOrderIndex(Long draftId);

    Optional<DiseaseDraftSection> findByDraftIdAndSectionTypeAndDeletedAtIsNull(Long draftId, DraftSectionType sectionType);
}
