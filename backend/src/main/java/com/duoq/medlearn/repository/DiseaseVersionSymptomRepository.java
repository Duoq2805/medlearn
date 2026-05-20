package com.duoq.medlearn.repository;

import com.duoq.medlearn.domain.entity.DiseaseVersionSymptom;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;

@Repository
public interface DiseaseVersionSymptomRepository extends JpaRepository<DiseaseVersionSymptom, Long> {

    // ===== BASIC CRUD =====

    List<DiseaseVersionSymptom> findAllByDiseaseVersionId(Long diseaseVersionId);

    List<DiseaseVersionSymptom> findAllBySymptomId(Long symptomId);

    boolean existsByDiseaseVersionIdAndSymptomId(Long diseaseVersionId, Long symptomId);

    @Modifying
    @Transactional
    void deleteByDiseaseVersionIdAndSymptomId(Long diseaseVersionId, Long symptomId);

    @Modifying
    @Transactional
    void deleteByDiseaseVersionId(Long diseaseVersionId);

    // ===== SYMPTOM CHECKER CORE =====

    @Query(value = """
        SELECT 
            d.id as diseaseId,
            d.name as diseaseName,
            d.slug as diseaseSlug,
            COUNT(DISTINCT dvs.symptom_id) as matchCount,
            SUM(dvs.weight_score) as totalWeight
        FROM disease_version dv
        JOIN disease d ON d.id = dv.disease_id
        JOIN disease_version_symptom dvs ON dvs.disease_version_id = dv.id
        WHERE dvs.symptom_id IN (:symptomIds)
          AND dv.id = d.current_version_id
          AND dv.status = 'APPROVED'
          AND dv.deleted_at IS NULL
          AND d.deleted_at IS NULL
        GROUP BY d.id, d.name, d.slug
        HAVING COUNT(DISTINCT dvs.symptom_id) > 0
        ORDER BY totalWeight DESC, matchCount DESC
        LIMIT :limit
        """, nativeQuery = true)
    List<Object[]> findMatchingDiseasesRaw(
            @Param("symptomIds") List<Long> symptomIds,
            @Param("limit") int limit
    );

    // Get matched symptom names for a specific disease
    @Query("""
    SELECT s.name
    FROM DiseaseVersionSymptom dvs
    JOIN dvs.symptom s
    WHERE dvs.diseaseVersion.id = (
        SELECT d.currentVersion.id
        FROM Disease d
        WHERE d.id = :diseaseId
    )
    AND s.id IN :symptomIds
    """)
    List<String> findMatchedSymptomsByDisease(
            @Param("diseaseId") Long diseaseId,
            @Param("symptomIds") List<Long> symptomIds
    );



    // ===== OPTIMIZED FOR SYMPTOM CHECKER (returns only IDs for better performance) =====
    @Query("""
    SELECT DISTINCT dvs.diseaseVersion.disease.id
    FROM DiseaseVersionSymptom dvs
    WHERE dvs.symptom.id IN :symptomIds
      AND dvs.diseaseVersion.status = 'APPROVED'
      AND dvs.diseaseVersion.id IN (
            SELECT d.currentVersion.id
            FROM Disease d
            WHERE d.currentVersion IS NOT NULL
      )
      AND dvs.diseaseVersion.deletedAt IS NULL
    GROUP BY dvs.diseaseVersion.disease.id
    HAVING COUNT(DISTINCT dvs.symptom.id) >= :minMatchCount
    """)
    Set<Long> findDiseaseIdsBySymptoms(
            @Param("symptomIds") List<Long> symptomIds,
            @Param("minMatchCount") int minMatchCount
    );

    // ===== BULK OPERATIONS =====

    @Modifying
    @Transactional
    @Query("""
        DELETE FROM DiseaseVersionSymptom dvs 
        WHERE dvs.diseaseVersion.id = :versionId 
          AND dvs.symptom.id IN :symptomIds
        """)
    void deleteAllByVersionIdAndSymptomIds(
            @Param("versionId") Long versionId,
            @Param("symptomIds") List<Long> symptomIds
    );

    // ===== STATISTICS =====

    @Query("SELECT AVG(dvs.weightScore) FROM DiseaseVersionSymptom dvs WHERE dvs.diseaseVersion.id = :versionId")
    Double getAverageWeightScoreForVersion(@Param("versionId") Long versionId);

    long countByDiseaseVersionId(Long diseaseVersionId);
}