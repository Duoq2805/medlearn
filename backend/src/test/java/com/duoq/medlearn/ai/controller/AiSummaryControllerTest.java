package com.duoq.medlearn.ai.controller;

import com.duoq.medlearn.ai.dto.response.SummaryResponse;
import com.duoq.medlearn.ai.dto.request.SummaryRequest;
import com.duoq.medlearn.ai.dto.enums.SummaryType;
import com.duoq.medlearn.ai.summary.AiSummaryService;
import com.duoq.medlearn.security.CurrentUserResolver;
import com.duoq.medlearn.security.JwtService;
import com.duoq.medlearn.security.OAuth2LoginSuccessHandler;
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
import org.springframework.http.MediaType;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserService;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.test.web.servlet.MockMvc;

import java.time.OffsetDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(
    controllers = AiSummaryController.class,
    excludeAutoConfiguration = {
        SecurityAutoConfiguration.class,
        SecurityFilterAutoConfiguration.class,
        OAuth2ClientAutoConfiguration.class,
        OAuth2ResourceServerAutoConfiguration.class,
        UserDetailsServiceAutoConfiguration.class
    }
)
class AiSummaryControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private AiSummaryService aiSummaryService;

    @MockBean
    private CurrentUserResolver currentUserResolver;

    @MockBean
    private UserDetailsService userDetailsService;

    @MockBean
    private JwtService jwtService;

    @MockBean
    @SuppressWarnings("unchecked")
    private OAuth2UserService<OAuth2UserRequest, OAuth2User> oAuth2UserService;

    @MockBean
    private OAuth2LoginSuccessHandler oAuth2LoginSuccessHandler;

    @Test
    void generate_shouldReturn201() throws Exception {
        var summary = SummaryResponse.builder()
                .id(1L).diseaseId(1L).diseaseName("Diabetes")
                .summaryType(SummaryType.STUDENT).version(1)
                .content("Generated content").totalTokens(150)
                .createdAt(OffsetDateTime.now()).build();

        when(currentUserResolver.resolveCurrentUserId()).thenReturn(42L);
        when(aiSummaryService.generate(eq(1L), any(SummaryRequest.class), eq(42L))).thenReturn(summary);

        mockMvc.perform(post("/api/ai/summaries/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"summaryType\":\"STUDENT\"}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.id").value(1))
                .andExpect(jsonPath("$.data.diseaseName").value("Diabetes"));
    }

    @Test
    void generate_shouldReturn400WhenTypeMissing() throws Exception {
        mockMvc.perform(post("/api/ai/summaries/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void listByDisease_shouldReturn200() throws Exception {
        var summaries = List.of(
                SummaryResponse.builder().id(1L).diseaseId(1L).summaryType(SummaryType.STUDENT).build(),
                SummaryResponse.builder().id(2L).diseaseId(1L).summaryType(SummaryType.CLINICAL).build()
        );
        when(aiSummaryService.listByDisease(1L)).thenReturn(summaries);

        mockMvc.perform(get("/api/ai/summaries/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.length()").value(2));
    }

    @Test
    void getLatest_shouldReturn200() throws Exception {
        var summary = SummaryResponse.builder()
                .id(1L).diseaseId(1L).summaryType(SummaryType.STUDENT).version(2).build();
        when(aiSummaryService.getLatest(1L, "STUDENT")).thenReturn(summary);

        mockMvc.perform(get("/api/ai/summaries/1/latest?type=STUDENT"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.version").value(2));
    }

    @Test
    void getLatest_shouldDefaultToStudentType() throws Exception {
        var summary = SummaryResponse.builder()
                .id(1L).diseaseId(1L).summaryType(SummaryType.STUDENT).version(1).build();
        when(aiSummaryService.getLatest(1L, "STUDENT")).thenReturn(summary);

        mockMvc.perform(get("/api/ai/summaries/1/latest"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    void getByVersion_shouldReturn200() throws Exception {
        var summary = SummaryResponse.builder()
                .id(1L).diseaseId(1L).summaryType(SummaryType.EXAM).version(1).build();
        when(aiSummaryService.getByVersion(1L, "EXAM", 1)).thenReturn(summary);

        mockMvc.perform(get("/api/ai/summaries/1/versions/1?type=EXAM"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.version").value(1));
    }
}
