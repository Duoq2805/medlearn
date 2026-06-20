package com.duoq.medlearn.repository;

import com.duoq.medlearn.domain.entity.Disease;
import com.duoq.medlearn.domain.dto.disease.DiseaseSummaryDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import jakarta.persistence.LockModeType;
import java.util.Optional;

@Repository
public interface DiseaseRepository extends JpaRepository<Disease, Long> {

    boolean existsByName(String name);

    boolean existsByNameAndIdNot(String name, Long id);

    Optional<Disease> findByName(String name);

    Optional<Disease> findBySlug(String slug);

    @Query("SELECT d FROM Disease d LEFT JOIN FETCH d.category WHERE d.slug = :slug AND d.deletedAt IS NULL")
    Optional<Disease> findBySlugWithCategory(@Param("slug") String slug);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT d FROM Disease d WHERE d.id = :id AND d.deletedAt IS NULL")
    Optional<Disease> findByIdForUpdate(@Param("id") Long id);

    // Method to find disease by ID bypassing @SQLRestriction for restore operations
    @Query("SELECT d FROM Disease d WHERE d.id = :id")
    Optional<Disease> findByIdIgnoreDeletedAt(@Param("id") Long id);

    Page<Disease> findAllByDeletedAtIsNull(Pageable pageable);

    Page<Disease> findAllByCategoryIdAndDeletedAtIsNull(Long categoryId, Pageable pageable);

    @Query("""
        SELECT new com.duoq.medlearn.domain.dto.disease.DiseaseSummaryDTO(
            d.id, d.name, d.slug, d.updatedAt
        )
        FROM Disease d
        WHERE d.deletedAt IS NULL
          AND (:name IS NULL OR LOWER(d.name) LIKE LOWER(CONCAT('%', :name, '%')))
        """)
    Page<DiseaseSummaryDTO> findSummaryByNameContaining(
            @Param("name") String name,
            Pageable pageable
    );

    @Query("""
    SELECT DISTINCT new com.duoq.medlearn.domain.dto.disease.DiseaseSummaryDTO(
        d.id, d.name, d.slug, d.updatedAt
    )
    FROM Disease d
    JOIN d.currentVersion dv
    LEFT JOIN DiseaseVersionSymptom dvs ON dvs.diseaseVersion.id = dv.id
    WHERE d.deletedAt IS NULL
      AND dv.status = 'APPROVED'
      AND dv.deletedAt IS NULL
      AND (:name IS NULL OR LOWER(d.name) LIKE LOWER(CONCAT('%', :name, '%')))
      AND (:categoryId IS NULL OR d.category.id = :categoryId)
      AND (:symptomIds IS NULL OR dvs.symptom.id IN :symptomIds)
    """)
    Page<DiseaseSummaryDTO> findApprovedSummaryByFilters(
            @Param("name") String name,
            @Param("categoryId") Long categoryId,
            @Param("symptomIds") java.util.List<Long> symptomIds,
            Pageable pageable
    );

    @Query("""
    SELECT DISTINCT new com.duoq.medlearn.domain.dto.disease.DiseaseSummaryDTO(
        d.id, d.name, d.slug, d.updatedAt
    )
    FROM Disease d
    JOIN d.currentVersion dv
    LEFT JOIN DiseaseVersionSymptom dvs ON dvs.diseaseVersion.id = dv.id
    WHERE d.deletedAt IS NULL
      AND dv.status = 'APPROVED'
      AND dv.deletedAt IS NULL
      AND (:name IS NULL OR LOWER(d.name) LIKE LOWER(CONCAT('%', :name, '%')))
      AND (:categoryId IS NULL OR d.category.id = :categoryId)
      AND (:symptomIds IS NULL OR dvs.symptom.id IN :symptomIds)
    """)
    Page<DiseaseSummaryDTO> findSummaryByApprovedFilters(
            @Param("name") String name,
            @Param("categoryId") Long categoryId,
            @Param("symptomIds") java.util.List<Long> symptomIds,
            Pageable pageable
    );

    // Query without symptom filter - used when symptomIds is null to avoid Hibernate type inference issue
    @Query("""
    SELECT DISTINCT new com.duoq.medlearn.domain.dto.disease.DiseaseSummaryDTO(
        d.id, d.name, d.slug, d.updatedAt
    )
    FROM Disease d
    JOIN d.currentVersion dv
    WHERE d.deletedAt IS NULL
      AND dv.status = 'APPROVED'
      AND dv.deletedAt IS NULL
      AND (:name IS NULL OR LOWER(d.name) LIKE LOWER(CONCAT('%', :name, '%')))
      AND (:categoryId IS NULL OR d.category.id = :categoryId)
    """)
    Page<DiseaseSummaryDTO> findApprovedSummaryByFiltersWithoutSymptomIds(
            @Param("name") String name,
            @Param("categoryId") Long categoryId,
            Pageable pageable
    );

    Page<DiseaseSummaryDTO> findApprovedSummaryByNameContaining(String keyword, Long categoryId, Pageable pageable);
}