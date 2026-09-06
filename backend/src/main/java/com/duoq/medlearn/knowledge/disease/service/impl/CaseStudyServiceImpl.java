package com.duoq.medlearn.service.impl;

import com.duoq.medlearn.domain.dto.casestudy.CaseStudyDetailResponse;
import com.duoq.medlearn.domain.dto.casestudy.CaseStudySummaryProjection;
import com.duoq.medlearn.domain.dto.casestudy.CreateCaseStudyRequest;
import com.duoq.medlearn.domain.dto.casestudy.DiagnoseRequest;
import com.duoq.medlearn.domain.dto.casestudy.DiagnoseResponse;
import com.duoq.medlearn.domain.entity.CaseStudy;
import com.duoq.medlearn.domain.entity.Symptom;
import com.duoq.medlearn.domain.entity.User;
import com.duoq.medlearn.domain.enums.CaseDifficulty;
import com.duoq.medlearn.domain.enums.ContentStatus;
import com.duoq.medlearn.exception.ResourceNotFoundException;
import com.duoq.medlearn.mapper.CaseStudyMapper;
import com.duoq.medlearn.repository.CaseStudyRepository;
import com.duoq.medlearn.repository.SymptomRepository;
import com.duoq.medlearn.repository.UserRepository;
import com.duoq.medlearn.security.CurrentUserResolver;
import com.duoq.medlearn.service.CaseStudyService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Set;

@Service
@RequiredArgsConstructor
public class CaseStudyServiceImpl implements CaseStudyService {
    private final CaseStudyRepository caseStudyRepository;
    private final SymptomRepository symptomRepository;
    private final CaseStudyMapper caseStudyMapper;
    private final CurrentUserResolver currentUserResolver;
    private final UserRepository userRepository;

    @Override
    @Transactional(readOnly = true)
    public Page<CaseStudySummaryProjection> getAllCases(Pageable pageable) {
        return caseStudyRepository.findAllByStatusAndDeletedAtIsNull(ContentStatus.APPROVED, pageable)
                .map(caseStudyMapper::toCaseStudySummaryProjection);
    }

    @Override
    @Transactional(readOnly = true)
    public CaseStudyDetailResponse getCaseById(Long id) {
        return caseStudyMapper.toCaseStudyDetailResponse(findCaseStudy(id));
    }

    @Override
    @Transactional(readOnly = true)
    public CaseStudyDetailResponse getCaseBySlug(String slug) {
        return caseStudyMapper.toCaseStudyDetailResponse(caseStudyRepository.findBySlug(slug)
                .orElseThrow(() -> new ResourceNotFoundException("Case study not found")));
    }

    @Override
    @Transactional
    public CaseStudyDetailResponse createCase(CreateCaseStudyRequest request) {
        String slug = request.getTitle().toLowerCase().replaceAll("[^a-z0-9]+", "-").replaceAll("^-|-$", "");
        if (slug.isEmpty()) slug = "case-" + System.currentTimeMillis();

        Set<Symptom> symptoms = request.getSymptomIds() != null
                ? Set.copyOf(symptomRepository.findAllByIdIn(request.getSymptomIds().stream().toList()))
                : Set.of();

        User currentUser = findCurrentUser();

        CaseStudy caseStudy = CaseStudy.builder()
                .title(request.getTitle())
                .slug(slug)
                .description(request.getDescription())
                .difficulty(request.getDifficulty() != null ? request.getDifficulty() : CaseDifficulty.MEDIUM)
                .diagnosis(request.getDiagnosis())
                .learningNotes(request.getLearningNotes())
                .patientAge(request.getPatientAge())
                .patientGender(request.getPatientGender())
                .chiefComplaint(request.getChiefComplaint())
                .caseQuestion(request.getCaseQuestion())
                .explanation(request.getExplanation())
                .status(ContentStatus.DRAFT)
                .symptoms(symptoms)
                .createdBy(currentUser)
                .build();
        return caseStudyMapper.toCaseStudyDetailResponse(caseStudyRepository.save(caseStudy));
    }

    @Override
    @Transactional(readOnly = true)
    public DiagnoseResponse submitDiagnosis(Long id, DiagnoseRequest request) {
        CaseStudy caseStudy = findCaseStudy(id);
        boolean correct = caseStudy.getDiagnosis() != null
                && caseStudy.getDiagnosis().trim().equalsIgnoreCase(request.getDiagnosis().trim());
        String feedback = correct
                ? "Correct diagnosis!"
                : "Incorrect. The expected diagnosis is: " + caseStudy.getDiagnosis();
        return new DiagnoseResponse(correct, feedback);
    }

    private CaseStudy findCaseStudy(Long id) {
        return caseStudyRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Case study not found"));
    }

    private User findCurrentUser() {
        Long userId = currentUserResolver.resolveCurrentUserId();
        return userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
    }
}
