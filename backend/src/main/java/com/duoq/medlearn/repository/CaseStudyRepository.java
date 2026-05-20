package com.duoq.medlearn.repository;

import com.duoq.medlearn.domain.entity.CaseStudy;
import com.duoq.medlearn.domain.enums.CaseDifficulty;
import com.duoq.medlearn.domain.enums.ContentStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CaseStudyRepository extends BaseRepository<CaseStudy, Long> {

    // ===== BASIC QUERIES =====

    Optional<CaseStudy> findBySlug(String slug);

    Page<CaseStudy> findAllByDeletedAtIsNull(Pageable pageable);

    // ===== STATUS-BASED QUERIES =====

    // Dùng ContentStatus thay vì DiseaseStatus
    Page<CaseStudy> findAllByStatusAndDeletedAtIsNull(ContentStatus status, Pageable pageable);

    Page<CaseStudy> findAllByStatusAndDifficultyAndDeletedAtIsNull(
            ContentStatus status, CaseDifficulty difficulty, Pageable pageable);

    List<CaseStudy> findAllByStatus(ContentStatus status);

    // ===== FEATURED & POPULAR =====

    Page<CaseStudy> findAllByIsFeaturedTrueAndDeletedAtIsNull(Pageable pageable);

    List<CaseStudy> findTop10ByIsFeaturedTrueAndDeletedAtIsNullOrderByViewCountDesc();

    // ===== CREATOR-BASED QUERIES =====

    Page<CaseStudy> findAllByCreatedByIdAndDeletedAtIsNull(Long userId, Pageable pageable);

    Page<CaseStudy> findAllByCreatedByIdAndStatusAndDeletedAtIsNull(
            Long userId, ContentStatus status, Pageable pageable);

    // ===== SEARCH QUERIES =====

    @Query("""
        SELECT cs FROM CaseStudy cs 
        WHERE cs.deletedAt IS NULL 
          AND cs.status = 'APPROVED'
          AND (LOWER(cs.title) LIKE LOWER(CONCAT('%', :keyword, '%'))
               OR LOWER(cs.description) LIKE LOWER(CONCAT('%', :keyword, '%')))
        """)
    Page<CaseStudy> searchApprovedCaseStudies(@Param("keyword") String keyword, Pageable pageable);

    // ===== STATISTICS =====

    long countByStatus(ContentStatus status);

    @Query("SELECT AVG(cs.viewCount) FROM CaseStudy cs WHERE cs.deletedAt IS NULL AND cs.status = 'APPROVED'")
    Double getAverageViewCount();
}