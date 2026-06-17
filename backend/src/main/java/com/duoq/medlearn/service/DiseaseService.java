package com.duoq.medlearn.service;

import com.duoq.medlearn.domain.dto.disease.DiseaseSummaryDTO;
import com.duoq.medlearn.domain.dto.disease.CreateDiseaseDraftRequest;
import com.duoq.medlearn.domain.dto.disease.CreateDiseaseRequest;
import com.duoq.medlearn.domain.dto.disease.DiseaseSearchRequest;
import com.duoq.medlearn.domain.dto.disease.UpdateDiseaseRequest;
import com.duoq.medlearn.domain.dto.disease.DiseaseDTO;
import com.duoq.medlearn.domain.dto.disease.DiseaseDetailDTO;
import com.duoq.medlearn.domain.dto.version.DiseaseVersionDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface DiseaseService {

    DiseaseDTO createDisease(CreateDiseaseRequest request);

    DiseaseDTO createDiseaseDraft(CreateDiseaseDraftRequest request);

    DiseaseDTO getDiseaseById(Long diseaseId);

    DiseaseDetailDTO getDiseaseBySlug(String slug);

    DiseaseDetailDTO getDiseaseCurrentVersion(Long diseaseId);

    Page<DiseaseSummaryDTO> getApprovedDiseases(
            String keyword,
            Long categoryId,
            List<Long> symptomIds,
            Pageable pageable
    );

    Page<DiseaseSummaryDTO> searchDiseases(
            DiseaseSearchRequest request,
            Pageable pageable
    );

    DiseaseDTO updateDiseaseMetadata(Long diseaseId, UpdateDiseaseRequest request);

    DiseaseVersionDTO cloneCurrentVersion(Long diseaseId);

    void softDeleteDisease(Long diseaseId);

    void restoreDisease(Long diseaseId);

    void assignCategory(Long diseaseId, Long categoryId);

    void removeCategory(Long diseaseId);
}
