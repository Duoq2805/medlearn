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

    // ===== SYMPTOM CHECKER V1 =====

    @Query("""
    SELECT COUNT(dvs)
    FROM DiseaseVersionSymptom dvs
    WHERE dvs.diseaseVersion.id = (
        SELECT d.currentVersion.id
        FROM Disease d
        WHERE d.id = :diseaseId
          AND d.currentVersion IS NOT NULL
    )
    """)
    Long findTotalSymptomCountByDiseaseId(@Param("diseaseId") Long diseaseId);

    @Query("""
    SELECT dvs.symptom.id
    FROM DiseaseVersionSymptom dvs
    WHERE dvs.diseaseVersion.id = (
        SELECT d.currentVersion.id
        FROM Disease d
        WHERE d.id = :diseaseId
          AND d.currentVersion IS NOT NULL
    )
    """)
    List<Long> findAllSymptomIdsByDiseaseId(@Param("diseaseId") Long diseaseId);

    // Batch fetch: total symptom count for multiple diseases at once
    @Query("""
    SELECT d.id as diseaseId, COUNT(dvs) as totalCount
    FROM Disease d
    JOIN DiseaseVersionSymptom dvs ON dvs.diseaseVersion.id = d.currentVersion.id
    WHERE d.id IN :diseaseIds
      AND d.currentVersion IS NOT NULL
      AND d.deletedAt IS NULL
    GROUP BY d.id
    """)
    List<Object[]> findTotalSymptomCountBatch(@Param("diseaseIds") List<Long> diseaseIds);

    // Batch fetch: all disease-to-symptom mappings for multiple diseases
    @Query("""
    SELECT d.id as diseaseId, dvs.symptom.id as symptomId
    FROM Disease d
    JOIN DiseaseVersionSymptom dvs ON dvs.diseaseVersion.id = d.currentVersion.id
    WHERE d.id IN :diseaseIds
      AND d.currentVersion IS NOT NULL
      AND d.deletedAt IS NULL
    """)
    List<Object[]> findDiseaseSymptomMappings(@Param("diseaseIds") List<Long> diseaseIds);

    @Query("""
    SELECT d.id, d.name, dvs.symptom.id, s.name, COALESCE(dvs.weightScore, 1)
    FROM Disease d
    JOIN DiseaseVersionSymptom dvs ON dvs.diseaseVersion.id = d.currentVersion.id
    JOIN dvs.symptom s
    WHERE d.id IN :diseaseIds
      AND d.currentVersion IS NOT NULL
      AND d.currentVersion.status = 'APPROVED'
      AND d.currentVersion.deletedAt IS NULL
      AND d.deletedAt IS NULL
    """)
    List<Object[]> findDiseaseSymptomDetailsForV2(@Param("diseaseIds") List<Long> diseaseIds);
}