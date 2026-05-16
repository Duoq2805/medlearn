package com.duoq.medlearn.repository;

import com.duoq.medlearn.domain.entity.DiseaseSection;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface DiseaseSectionRepository extends JpaRepository<DiseaseSection, Long> {

    // Lấy sections của một version, sắp xếp theo thứ tự, chỉ lấy chưa xóa
    List<DiseaseSection> findAllByDiseaseVersionIdAndIsDeletedFalseOrderByOrderIndexAsc(Long diseaseVersionId);

    // Lấy tất cả sections của một version (kể cả đã xóa, dùng khi clone)
    List<DiseaseSection> findAllByDiseaseVersionId(Long diseaseVersionId);

    // =============================================
    // Dùng cho Symptom Checker
    // =============================================

    /**
     * Lấy mô tả ngắn của disease (section 'definition')
     * Trả về Optional để xử lý trường hợp không có description
     */
    @Query("""
        SELECT ds.content
        FROM DiseaseSection ds
        JOIN ds.diseaseVersion dv
        JOIN dv.disease d
        WHERE d.id = :diseaseId
          AND dv.isCurrent = true
          AND dv.isDeleted = false
          AND ds.sectionType.name = 'definition'
          AND ds.isDeleted = false
        """)
    Optional<String> findShortDescriptionByDiseaseId(@Param("diseaseId") Long diseaseId);

    // Bonus: Lấy toàn bộ nội dung của disease để hiển thị chi tiết
    @Query("""
        SELECT ds
        FROM DiseaseSection ds
        JOIN FETCH ds.sectionType st
        WHERE ds.diseaseVersion.id = :versionId
          AND ds.isDeleted = false
        ORDER BY ds.orderIndex ASC
        """)
    List<DiseaseSection> findAllByVersionIdWithType(@Param("versionId") Long versionId);
}