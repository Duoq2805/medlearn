package com.duoq.medlearn.controller;
import com.duoq.medlearn.knowledge.disease.controller.CaseStudyController;

import com.duoq.medlearn.knowledge.disease.dto.response.CaseStudyDetailResponse;
import com.duoq.medlearn.knowledge.disease.dto.projection.CaseStudySummaryProjection;
import com.duoq.medlearn.knowledge.disease.dto.request.CreateCaseStudyRequest;
import com.duoq.medlearn.knowledge.disease.dto.request.DiagnoseRequest;
import com.duoq.medlearn.knowledge.disease.dto.response.DiagnoseResponse;
import com.duoq.medlearn.common.dto.PagedResponse;
import com.duoq.medlearn.knowledge.disease.enums.CaseDifficulty;
import com.duoq.medlearn.common.exception.ResourceNotFoundException;
import com.duoq.medlearn.common.security.JwtService;
import com.duoq.medlearn.auth.security.OAuth2LoginSuccessHandler;
import com.duoq.medlearn.knowledge.disease.service.CaseStudyService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.security.oauth2.client.servlet.OAuth2ClientAutoConfiguration;
import org.springframework.boot.autoconfigure.security.oauth2.resource.servlet.OAuth2ResourceServerAutoConfiguration;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.boot.autoconfigure.security.servlet.SecurityFilterAutoConfiguration;
import org.springframework.boot.autoconfigure.security.servlet.UserDetailsServiceAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.MediaType;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserService;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.test.web.servlet.MockMvc;

import java.time.OffsetDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(
    controllers = CaseStudyController.class,
    excludeAutoConfiguration = {
        SecurityAutoConfiguration.class,
        SecurityFilterAutoConfiguration.class,
        OAuth2ClientAutoConfiguration.class,
        OAuth2ResourceServerAutoConfiguration.class,
        UserDetailsServiceAutoConfiguration.class
    }
)
class CaseStudyControllerTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private ObjectMapper objectMapper;

    @MockBean private CaseStudyService caseStudyService;
    @MockBean private JwtService jwtService;
    @MockBean private UserDetailsService userDetailsService;
    @MockBean private OAuth2LoginSuccessHandler oAuth2LoginSuccessHandler;
    @MockBean private OAuth2UserService<OAuth2UserRequest, OAuth2User> oAuth2UserService;

    private final CaseStudyDetailResponse testDetail = CaseStudyDetailResponse.builder()
            .id(1L)
            .title("Test Case")
            .slug("test-case")
            .description("Description")
            .difficulty(CaseDifficulty.MEDIUM)
            .diagnosis("Diabetes")
            .createdBy(1L)
            .build();

    private final CaseStudySummaryProjection testSummary = CaseStudySummaryProjection.builder()
            .id(1L)
            .title("Test Case")
            .slug("test-case")
            .difficulty(CaseDifficulty.MEDIUM)
            .build();

    @Test
    void getAllCases_shouldReturn200() throws Exception {
        Page<CaseStudySummaryProjection> page = new PageImpl<>(List.of(testSummary));
        when(caseStudyService.getAllCases(any(PageRequest.class))).thenReturn(page);

        mockMvc.perform(get("/api/cases"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.content[0].title").value("Test Case"));
    }

    @Test
    void getCaseById_shouldReturn200_whenFound() throws Exception {
        when(caseStudyService.getCaseById(1L)).thenReturn(testDetail);

        mockMvc.perform(get("/api/cases/{id}", 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.title").value("Test Case"));
    }

    @Test
    void getCaseById_shouldReturn404_whenNotFound() throws Exception {
        when(caseStudyService.getCaseById(999L)).thenThrow(new ResourceNotFoundException("Case study not found"));

        mockMvc.perform(get("/api/cases/{id}", 999L))
                .andExpect(status().isNotFound());
    }

    @Test
    void getCaseBySlug_shouldReturn200() throws Exception {
        when(caseStudyService.getCaseBySlug("test-case")).thenReturn(testDetail);

        mockMvc.perform(get("/api/cases/slug/{slug}", "test-case"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.title").value("Test Case"));
    }

    @Test
    void getCaseBySlug_shouldReturn404_whenNotFound() throws Exception {
        when(caseStudyService.getCaseBySlug("unknown")).thenThrow(new ResourceNotFoundException("Case study not found"));

        mockMvc.perform(get("/api/cases/slug/{slug}", "unknown"))
                .andExpect(status().isNotFound());
    }

    @Test
    void createCase_shouldReturn200() throws Exception {
        CreateCaseStudyRequest request = new CreateCaseStudyRequest();
        request.setTitle("New Case");
        request.setDiagnosis("Diabetes");

        when(caseStudyService.createCase(any())).thenReturn(testDetail);

        mockMvc.perform(post("/api/cases")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.title").value("Test Case"))
                .andExpect(jsonPath("$.message").value("Case study created"));
    }

    @Test
    void createCase_shouldReturn400_whenTitleBlank() throws Exception {
        CreateCaseStudyRequest request = new CreateCaseStudyRequest();
        request.setTitle("");

        mockMvc.perform(post("/api/cases")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void submitDiagnosis_shouldReturn200_whenCorrect() throws Exception {
        DiagnoseRequest request = new DiagnoseRequest();
        request.setDiagnosis("Diabetes");

        when(caseStudyService.submitDiagnosis(eq(1L), any())).thenReturn(new DiagnoseResponse(true, "Correct diagnosis!"));

        mockMvc.perform(post("/api/cases/{id}/diagnose", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.correct").value(true));
    }

    @Test
    void submitDiagnosis_shouldReturn200_whenIncorrect() throws Exception {
        DiagnoseRequest request = new DiagnoseRequest();
        request.setDiagnosis("Flu");

        when(caseStudyService.submitDiagnosis(eq(1L), any())).thenReturn(new DiagnoseResponse(false, "Incorrect."));

        mockMvc.perform(post("/api/cases/{id}/diagnose", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.correct").value(false));
    }
}
