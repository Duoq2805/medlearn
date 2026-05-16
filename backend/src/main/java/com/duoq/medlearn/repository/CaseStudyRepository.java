package com.duoq.medlearn.repository;

import com.duoq.medlearn.domain.entity.CaseStudy;
import com.duoq.medlearn.domain.enums.CaseDifficulty;
import com.duoq.medlearn.domain.enums.DiseaseStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;


@Repository
public interface CaseStudyRepository extends BaseRepository<CaseStudy, Long> {

    // Lấy danh sách case study theo status
    Page<CaseStudy> findAllByStatusAndIsDeletedFalse(DiseaseStatus status, Pageable pageable);

    // Lấy danh sách case study theo status và độ khó
    Page<CaseStudy> findAllByStatusAndDifficultyAndIsDeletedFalse(
            DiseaseStatus status, CaseDifficulty difficulty, Pageable pageable);

}