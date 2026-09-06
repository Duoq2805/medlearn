package com.duoq.medlearn.service;

import com.duoq.medlearn.domain.dto.disease.DiseaseSummaryProjection;
import com.duoq.medlearn.domain.dto.disease.CreateDiseaseDraftRequest;
import com.duoq.medlearn.domain.dto.disease.CreateDiseaseRequest;
import com.duoq.medlearn.domain.dto.disease.DiseaseSearchRequest;
import com.duoq.medlearn.domain.dto.disease.UpdateDiseaseRequest;
import com.duoq.medlearn.domain.dto.disease.DiseaseResponse;
import com.duoq.medlearn.domain.dto.disease.DiseaseDetailResponse;
import com.duoq.medlearn.domain.dto.version.DiseaseVersionResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface DiseaseService {

    DiseaseResponse createDisease(CreateDiseaseRequest request);

    DiseaseResponse createDiseaseDraft(CreateDiseaseDraftRequest request);

    DiseaseResponse getDiseaseById(Long diseaseId);

    DiseaseDetailResponse getDiseaseBySlug(String slug);

    DiseaseDetailResponse getDiseaseCurrentVersion(Long diseaseId);

    Page<DiseaseSummaryProjection> getApprovedDiseases(
            String keyword,
            Long categoryId,
            List<Long> symptomIds,
            Pageable pageable
    );

    Page<DiseaseSummaryProjection> searchDiseases(
            DiseaseSearchRequest request,
            Pageable pageable
    );

    DiseaseResponse updateDiseaseMetadata(Long diseaseId, UpdateDiseaseRequest request);

    DiseaseVersionResponse cloneCurrentVersion(Long diseaseId);

    void softDeleteDisease(Long diseaseId);

    void restoreDisease(Long diseaseId);

    void assignCategory(Long diseaseId, Long categoryId);

    void removeCategory(Long diseaseId);
}
