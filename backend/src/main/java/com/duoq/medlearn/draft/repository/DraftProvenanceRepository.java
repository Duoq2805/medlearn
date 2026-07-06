package com.duoq.medlearn.draft.repository;

import com.duoq.medlearn.draft.entity.DraftProvenance;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DraftProvenanceRepository extends JpaRepository<DraftProvenance, Long> {

    List<DraftProvenance> findAllByDraftId(Long draftId);
}
