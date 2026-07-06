package com.duoq.medlearn.draft.repository;

import com.duoq.medlearn.draft.entity.DraftSource;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DraftSourceRepository extends JpaRepository<DraftSource, Long> {

    List<DraftSource> findAllByDraftSectionId(Long draftSectionId);

    List<DraftSource> findAllByDocumentChunkId(Long documentChunkId);
}
