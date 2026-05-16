package com.duoq.medlearn.repository;

import com.duoq.medlearn.domain.entity.DiseaseVersionSymptom;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DiseaseVersionSymptomRepository extends JpaRepository<DiseaseVersionSymptom, Long> {

    // Lấy tất cả symptom mapping của một version
    List<DiseaseVersionSymptom> findAllByDiseaseVersionId(Long diseaseVersionId);

    // Kiểm tra một symptom đã được map với version chưa
    boolean existsByDiseaseVersionIdAndSymptomId(Long diseaseVersionId, Long symptomId);

    // Xóa một symptom khỏi version
    void deleteByDiseaseVersionIdAndSymptomId(Long diseaseVersionId, Long symptomId);

    // =============================================
    // SYMPTOM CHECKER QUERIES
    // =============================================

    /**
     * Tìm các disease match với danh sách symptom được chọn
     * Trả về: diseaseId, diseaseName, diseaseSlug, matchCount, totalWeight
     */
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
          AND dv.is_current = true
          AND dv.status = 'APPROVED'
          AND dv.is_deleted = false
          AND d.is_deleted = false
        GROUP BY d.id, d.name, d.slug
        HAVING COUNT(DISTINCT dvs.symptom_id) > 0
        ORDER BY totalWeight DESC, matchCount DESC
        LIMIT :limit
        """, nativeQuery = true)
    List<Object[]> findMatchingDiseasesRaw(
            @Param("symptomIds") List<Long> symptomIds,
            @Param("limit") int limit
    );

    /**
     * Lấy danh sách tên symptom đã match của một disease cụ thể
     * Dùng để hiển thị "Các triệu chứng phù hợp: sốt, ho, đau đầu..."
     */
    @Query("""
        SELECT s.name
        FROM DiseaseVersionSymptom dvs
        JOIN dvs.symptom s
        WHERE dvs.diseaseVersion.disease.id = :diseaseId
          AND dvs.diseaseVersion.isCurrent = true
          AND s.id IN :symptomIds
        """)
    List<String> findMatchedSymptomsByDisease(
            @Param("diseaseId") Long diseaseId,
            @Param("symptomIds") List<Long> symptomIds
    );
}