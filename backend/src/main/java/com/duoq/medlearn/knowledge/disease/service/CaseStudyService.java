package com.duoq.medlearn.knowledge.disease.service;

import com.duoq.medlearn.knowledge.disease.dto.response.CaseStudyDetailResponse;
import com.duoq.medlearn.knowledge.disease.dto.projection.CaseStudySummaryProjection;
import com.duoq.medlearn.knowledge.disease.dto.request.CreateCaseStudyRequest;
import com.duoq.medlearn.knowledge.disease.dto.request.DiagnoseRequest;
import com.duoq.medlearn.knowledge.disease.dto.response.DiagnoseResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface CaseStudyService {
    Page<CaseStudySummaryProjection> getAllCases(Pageable pageable);

    CaseStudyDetailResponse getCaseById(Long id);

    CaseStudyDetailResponse getCaseBySlug(String slug);

    CaseStudyDetailResponse createCase(CreateCaseStudyRequest request);

    DiagnoseResponse submitDiagnosis(Long id, DiagnoseRequest request);
}
