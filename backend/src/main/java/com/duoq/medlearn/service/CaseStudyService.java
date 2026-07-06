package com.duoq.medlearn.service;

import com.duoq.medlearn.domain.dto.casestudy.CaseStudyDetailResponse;
import com.duoq.medlearn.domain.dto.casestudy.CaseStudySummaryProjection;
import com.duoq.medlearn.domain.dto.casestudy.CreateCaseStudyRequest;
import com.duoq.medlearn.domain.dto.casestudy.DiagnoseRequest;
import com.duoq.medlearn.domain.dto.casestudy.DiagnoseResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface CaseStudyService {
    Page<CaseStudySummaryProjection> getAllCases(Pageable pageable);

    CaseStudyDetailResponse getCaseById(Long id);

    CaseStudyDetailResponse getCaseBySlug(String slug);

    CaseStudyDetailResponse createCase(CreateCaseStudyRequest request);

    DiagnoseResponse submitDiagnosis(Long id, DiagnoseRequest request);
}
