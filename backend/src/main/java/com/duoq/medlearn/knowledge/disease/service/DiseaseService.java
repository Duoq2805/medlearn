package com.duoq.medlearn.knowledge.disease.service;

import com.duoq.medlearn.knowledge.disease.dto.projection.DiseaseSummaryProjection;
import com.duoq.medlearn.knowledge.disease.dto.request.CreateDiseaseDraftRequest;
import com.duoq.medlearn.knowledge.disease.dto.request.CreateDiseaseRequest;
import com.duoq.medlearn.knowledge.disease.dto.request.DiseaseSearchRequest;
import com.duoq.medlearn.knowledge.disease.dto.request.UpdateDiseaseRequest;
import com.duoq.medlearn.knowledge.disease.dto.response.DiseaseResponse;
import com.duoq.medlearn.knowledge.disease.dto.response.DiseaseDetailResponse;
import com.duoq.medlearn.knowledge.version.dto.response.DiseaseVersionResponse;
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
