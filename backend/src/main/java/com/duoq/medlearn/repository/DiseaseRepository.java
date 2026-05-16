package com.duoq.medlearn.repository;

import com.duoq.medlearn.domain.entity.Disease;
import com.duoq.medlearn.domain.enums.DiseaseStatus;
import com.duoq.medlearn.dto.DiseaseSummaryDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface DiseaseRepository extends BaseRepository<Disease, Long> {

    // Kiểm tra tên đã tồn tại chưa
    boolean existsByName(String name);

    // Lấy danh sách disease đã approved, hỗ trợ search theo tên (case-insensitive)
    @Query("""
        SELECT new com.duoq.medlearn.dto.DiseaseSummaryDTO(
            d.id, d.name, d.slug, d.status, d.updatedAt
        )
        FROM Disease d 
        WHERE d.isDeleted = false 
          AND d.status = :status
          AND (:name IS NULL OR LOWER(d.name) LIKE LOWER(CONCAT('%', :name, '%')))
        """)
    Page<DiseaseSummaryDTO> findSummaryByStatusAndNameContaining(
            @Param("status") DiseaseStatus status,
            @Param("name") String name,
            Pageable pageable
    );

    // Lấy toàn bộ disease chưa bị xóa (dành cho admin/reviewer)
    Page<Disease> findAllByIsDeletedFalse(Pageable pageable);

}