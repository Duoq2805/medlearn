package com.duoq.medlearn.repository;

import com.duoq.medlearn.domain.entity.DiseaseVersion;
import com.duoq.medlearn.domain.enums.VersionStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface DiseaseVersionRepository extends JpaRepository<DiseaseVersion, Long> {

    // Tìm version hiện tại đang active của 1 disease
    Optional<DiseaseVersion> findByDiseaseIdAndIsCurrentTrueAndIsDeletedFalse(Long diseaseId);

    // Tìm version theo status (dùng cho moderation queue)
    Page<DiseaseVersion> findAllByStatusAndIsDeletedFalse(VersionStatus status, Pageable pageable);

    // Lấy toàn bộ version của 1 disease (history)
    List<DiseaseVersion> findAllByDiseaseIdAndIsDeletedFalseOrderByVersionNumberDesc(Long diseaseId);

    // Lấy version mới nhất của disease để tính version_number tiếp theo
    @Query("SELECT MAX(dv.versionNumber) FROM DiseaseVersion dv WHERE dv.disease.id = :diseaseId")
    Optional<Integer> findMaxVersionNumberByDiseaseId(@Param("diseaseId") Long diseaseId);

    // Tìm draft của contributor để edit
    Optional<DiseaseVersion> findByIdAndCreatedByIdAndStatus(Long id, Long createdById, VersionStatus status);

    // Lấy danh sách draft của contributor
    List<DiseaseVersion> findAllByCreatedByIdAndStatusAndIsDeletedFalse(Long createdById, VersionStatus status);
}