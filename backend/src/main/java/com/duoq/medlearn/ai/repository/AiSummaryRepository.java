package com.duoq.medlearn.ai.repository;

import com.duoq.medlearn.ai.enums.SummaryType;
import com.duoq.medlearn.ai.summary.entity.AiSummary;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AiSummaryRepository extends JpaRepository<AiSummary, Long> {

    List<AiSummary> findAllByDiseaseIdAndDeletedAtIsNullOrderByCreatedAtDesc(Long diseaseId);

    List<AiSummary> findAllByDiseaseIdAndSummaryTypeAndDeletedAtIsNullOrderByCreatedAtDesc(
            Long diseaseId, SummaryType summaryType);

    Optional<AiSummary> findTopByDiseaseIdAndSummaryTypeAndDeletedAtIsNullOrderByCreatedAtDesc(
            Long diseaseId, SummaryType summaryType);

    Optional<AiSummary> findByDiseaseIdAndSummaryTypeAndVersionAndDeletedAtIsNull(
            Long diseaseId, SummaryType summaryType, Integer version);

    @Query("SELECT COALESCE(MAX(s.version), 0) FROM AiSummary s WHERE s.disease.id = :diseaseId AND s.summaryType = :summaryType")
    int findMaxVersionByDiseaseIdAndSummaryType(
            @Param("diseaseId") Long diseaseId,
            @Param("summaryType") SummaryType summaryType);

    boolean existsByDiseaseIdAndSummaryTypeAndVersion(Long diseaseId, SummaryType summaryType, Integer version);
}
