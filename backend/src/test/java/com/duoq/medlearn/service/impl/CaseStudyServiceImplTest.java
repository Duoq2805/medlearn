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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CaseStudyServiceImplTest {

    @Mock private CaseStudyRepository caseStudyRepository;
    @Mock private SymptomRepository symptomRepository;
    @Mock private CaseStudyMapper caseStudyMapper;
    @Mock private CurrentUserResolver currentUserResolver;
    @Mock private UserRepository userRepository;

    @InjectMocks private CaseStudyServiceImpl caseStudyService;

    private User testUser;
    private CaseStudy testCaseStudy;
    private Symptom testSymptom;
    private CaseStudyDetailResponse testDetailDTO;
    private CaseStudySummaryProjection testSummaryResponse;
    private final Pageable pageable = PageRequest.of(0, 10);

    @BeforeEach
    void setUp() {
        testUser = User.builder()
                .id(1L)
                .username("doctor")
                .email("doctor@test.com")
                .build();

        testSymptom = Symptom.builder()
                .id(1L)
                .name("Fever")
                .slug("fever")
                .build();

        testCaseStudy = CaseStudy.builder()
                .id(1L)
                .title("Test Case")
                .slug("test-case")
                .description("A test case study")
                .difficulty(CaseDifficulty.MEDIUM)
                .diagnosis("Diabetes")
                .status(ContentStatus.APPROVED)
                .createdBy(testUser)
                .symptoms(Set.of(testSymptom))
                .build();

        testDetailDTO = CaseStudyDetailResponse.builder()
                .id(1L)
                .title("Test Case")
                .slug("test-case")
                .description("A test case study")
                .difficulty(CaseDifficulty.MEDIUM)
                .diagnosis("Diabetes")
                .createdBy(1L)
                .symptomIds(Set.of(1L))
                .build();

        testSummaryResponse = CaseStudySummaryProjection.builder()
                .id(1L)
                .title("Test Case")
                .slug("test-case")
                .description("A test case study")
                .difficulty(CaseDifficulty.MEDIUM)
                .build();
    }

    @Test
    void getAllCases_shouldReturnPagedResults() {
        Page<CaseStudy> page = new PageImpl<>(List.of(testCaseStudy), pageable, 1);
        when(caseStudyRepository.findAllByStatusAndDeletedAtIsNull(ContentStatus.APPROVED, pageable))
                .thenReturn(page);
        when(caseStudyMapper.toCaseStudySummaryProjection(testCaseStudy)).thenReturn(testSummaryResponse);

        Page<CaseStudySummaryProjection> result = caseStudyService.getAllCases(pageable);

        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).getTitle()).isEqualTo("Test Case");
    }

    @Test
    void getAllCases_shouldReturnEmptyPage_whenNoCases() {
        Page<CaseStudy> emptyPage = Page.empty(pageable);
        when(caseStudyRepository.findAllByStatusAndDeletedAtIsNull(ContentStatus.APPROVED, pageable))
                .thenReturn(emptyPage);

        Page<CaseStudySummaryProjection> result = caseStudyService.getAllCases(pageable);

        assertThat(result.getContent()).isEmpty();
    }

    @Test
    void getCaseById_shouldReturnDetail_whenFound() {
        when(caseStudyRepository.findById(1L)).thenReturn(Optional.of(testCaseStudy));
        when(caseStudyMapper.toCaseStudyDetailResponse(testCaseStudy)).thenReturn(testDetailDTO);

        CaseStudyDetailResponse result = caseStudyService.getCaseById(1L);

        assertThat(result).isNotNull();
        assertThat(result.getTitle()).isEqualTo("Test Case");
    }

    @Test
    void getCaseById_shouldThrow_whenNotFound() {
        when(caseStudyRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> caseStudyService.getCaseById(999L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Case study not found");
    }

    @Test
    void getCaseBySlug_shouldReturnDetail_whenFound() {
        when(caseStudyRepository.findBySlug("test-case")).thenReturn(Optional.of(testCaseStudy));
        when(caseStudyMapper.toCaseStudyDetailResponse(testCaseStudy)).thenReturn(testDetailDTO);

        CaseStudyDetailResponse result = caseStudyService.getCaseBySlug("test-case");

        assertThat(result).isNotNull();
        assertThat(result.getTitle()).isEqualTo("Test Case");
    }

    @Test
    void getCaseBySlug_shouldThrow_whenNotFound() {
        when(caseStudyRepository.findBySlug("unknown")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> caseStudyService.getCaseBySlug("unknown"))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Case study not found");
    }

    @Test
    void createCase_shouldCreateWithAllFields() {
        CreateCaseStudyRequest request = new CreateCaseStudyRequest();
        request.setTitle("New Case");
        request.setDescription("Description");
        request.setDifficulty(CaseDifficulty.HARD);
        request.setDiagnosis("Cancer");
        request.setSymptomIds(Set.of(1L));

        when(currentUserResolver.resolveCurrentUserId()).thenReturn(1L);
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(symptomRepository.findAllByIdIn(List.of(1L))).thenReturn(List.of(testSymptom));
        when(caseStudyRepository.save(any(CaseStudy.class))).thenReturn(testCaseStudy);
        when(caseStudyMapper.toCaseStudyDetailResponse(testCaseStudy)).thenReturn(testDetailDTO);

        CaseStudyDetailResponse result = caseStudyService.createCase(request);

        assertThat(result).isNotNull();
        verify(caseStudyRepository).save(argThat(cs -> {
            assertThat(cs.getTitle()).isEqualTo("New Case");
            assertThat(cs.getSlug()).isEqualTo("new-case");
            assertThat(cs.getStatus()).isEqualTo(ContentStatus.DRAFT);
            assertThat(cs.getCreatedBy()).isEqualTo(testUser);
            assertThat(cs.getDifficulty()).isEqualTo(CaseDifficulty.HARD);
            assertThat(cs.getSymptoms()).contains(testSymptom);
            return true;
        }));
    }

    @Test
    void createCase_shouldDefaultDifficultyToMedium() {
        CreateCaseStudyRequest request = new CreateCaseStudyRequest();
        request.setTitle("Another Case");
        request.setSymptomIds(null);

        when(currentUserResolver.resolveCurrentUserId()).thenReturn(1L);
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(caseStudyRepository.save(any(CaseStudy.class))).thenReturn(testCaseStudy);
        when(caseStudyMapper.toCaseStudyDetailResponse(testCaseStudy)).thenReturn(testDetailDTO);

        caseStudyService.createCase(request);

        verify(caseStudyRepository).save(argThat(cs -> {
            assertThat(cs.getDifficulty()).isEqualTo(CaseDifficulty.MEDIUM);
            assertThat(cs.getSymptoms()).isEmpty();
            assertThat(cs.getCreatedBy()).isEqualTo(testUser);
            return true;
        }));
    }

    @Test
    void createCase_shouldGenerateSlugFromTitle() {
        CreateCaseStudyRequest request = new CreateCaseStudyRequest();
        request.setTitle("A Complex Disease: Case #1");

        when(currentUserResolver.resolveCurrentUserId()).thenReturn(1L);
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(caseStudyRepository.save(any(CaseStudy.class))).thenReturn(testCaseStudy);
        when(caseStudyMapper.toCaseStudyDetailResponse(testCaseStudy)).thenReturn(testDetailDTO);

        caseStudyService.createCase(request);

        verify(caseStudyRepository).save(argThat(cs -> {
            assertThat(cs.getSlug()).isEqualTo("a-complex-disease-case-1");
            return true;
        }));
    }

    @Test
    void submitDiagnosis_shouldReturnCorrect_whenMatchExact() {
        when(caseStudyRepository.findById(1L)).thenReturn(Optional.of(testCaseStudy));

        DiagnoseRequest request = new DiagnoseRequest();
        request.setDiagnosis("Diabetes");

        DiagnoseResponse response = caseStudyService.submitDiagnosis(1L, request);

        assertThat(response.isCorrect()).isTrue();
    }

    @Test
    void submitDiagnosis_shouldReturnCorrect_whenCaseInsensitiveAndTrimmed() {
        when(caseStudyRepository.findById(1L)).thenReturn(Optional.of(testCaseStudy));

        DiagnoseRequest request = new DiagnoseRequest();
        request.setDiagnosis("  diabetes  ");

        DiagnoseResponse response = caseStudyService.submitDiagnosis(1L, request);

        assertThat(response.isCorrect()).isTrue();
    }

    @Test
    void submitDiagnosis_shouldReturnIncorrect_whenMismatch() {
        when(caseStudyRepository.findById(1L)).thenReturn(Optional.of(testCaseStudy));

        DiagnoseRequest request = new DiagnoseRequest();
        request.setDiagnosis("Flu");

        DiagnoseResponse response = caseStudyService.submitDiagnosis(1L, request);

        assertThat(response.isCorrect()).isFalse();
        assertThat(response.getFeedback()).contains("Incorrect");
    }

    @Test
    void submitDiagnosis_shouldReturnIncorrect_whenDiagnosisNull() {
        testCaseStudy.setDiagnosis(null);
        when(caseStudyRepository.findById(1L)).thenReturn(Optional.of(testCaseStudy));

        DiagnoseRequest request = new DiagnoseRequest();
        request.setDiagnosis("Anything");

        DiagnoseResponse response = caseStudyService.submitDiagnosis(1L, request);

        assertThat(response.isCorrect()).isFalse();
    }
}
