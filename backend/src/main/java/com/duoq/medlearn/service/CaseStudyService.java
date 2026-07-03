package com.duoq.medlearn.service;

import com.duoq.medlearn.domain.dto.casestudy.CaseStudyDetailDTO;
import com.duoq.medlearn.domain.dto.casestudy.CaseStudySummaryDTO;
import com.duoq.medlearn.domain.dto.casestudy.CreateCaseStudyRequest;
import com.duoq.medlearn.domain.dto.casestudy.DiagnoseRequest;
import com.duoq.medlearn.domain.dto.casestudy.DiagnoseResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface CaseStudyService {
    Page<CaseStudySummaryDTO> getAllCases(Pageable pageable);

    CaseStudyDetailDTO getCaseById(Long id);

    CaseStudyDetailDTO getCaseBySlug(String slug);

    CaseStudyDetailDTO createCase(CreateCaseStudyRequest request);

    DiagnoseResponse submitDiagnosis(Long id, DiagnoseRequest request);
}
