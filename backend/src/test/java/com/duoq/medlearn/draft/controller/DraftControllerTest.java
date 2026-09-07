package com.duoq.medlearn.draft.controller;

import com.duoq.medlearn.ai.draft.AiDraftGenerator;
import com.duoq.medlearn.draft.dto.request.*;
import com.duoq.medlearn.draft.dto.response.*;
import com.duoq.medlearn.draft.enums.DraftMethod;
import com.duoq.medlearn.draft.enums.DraftSectionType;
import com.duoq.medlearn.draft.enums.DraftStatus;
import com.duoq.medlearn.draft.service.DraftLifecycleService;
import com.duoq.medlearn.draft.service.DraftService;
import com.duoq.medlearn.common.security.CurrentUserResolver;
import com.duoq.medlearn.common.security.JwtService;
import com.duoq.medlearn.auth.security.OAuth2LoginSuccessHandler;
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
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
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
    controllers = {DraftController.class, AiDraftController.class},
    excludeAutoConfiguration = {
        SecurityAutoConfiguration.class,
        SecurityFilterAutoConfiguration.class,
        OAuth2ClientAutoConfiguration.class,
        OAuth2ResourceServerAutoConfiguration.class,
        UserDetailsServiceAutoConfiguration.class
    }
)
class DraftControllerTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private ObjectMapper objectMapper;

    @MockBean private DraftService draftService;
    @MockBean private DraftLifecycleService draftLifecycleService;
    @MockBean private AiDraftGenerator aiDraftGenerator;
    @MockBean private CurrentUserResolver currentUserResolver;
    @MockBean private JwtService jwtService;
    @MockBean private UserDetailsService userDetailsService;
    @MockBean private OAuth2LoginSuccessHandler oAuth2LoginSuccessHandler;
    @MockBean private OAuth2UserService<OAuth2UserRequest, OAuth2User> oAuth2UserService;

    private final DiseaseDraftResponse testDraft = DiseaseDraftResponse.builder()
            .id(1L).title("Test Draft").status(DraftStatus.DRAFT)
            .sourceMethod(DraftMethod.MANUAL).createdBy(1L)
            .createdAt(OffsetDateTime.now()).updatedAt(OffsetDateTime.now())
            .sections(List.of()).build();

    @Test
    void create_shouldReturn200() throws Exception {
        when(currentUserResolver.resolveCurrentUserId()).thenReturn(1L);
        when(draftService.create(any(CreateDraftRequest.class), eq(1L))).thenReturn(testDraft);

        var body = objectMapper.writeValueAsString(
                new CreateDraftRequest(null, "Test Draft", null, null, List.of()));

        mockMvc.perform(post("/api/drafts")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Draft created"));
    }

    @Test
    void listDrafts_shouldReturn200() throws Exception {
        when(currentUserResolver.resolveCurrentUserId()).thenReturn(1L);
        var page = new PageImpl<>(List.of(testDraft));
        when(draftService.listByUser(eq(1L), any(Pageable.class))).thenReturn(page);

        mockMvc.perform(get("/api/drafts"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    void listDrafts_shouldFilterByDisease() throws Exception {
        when(currentUserResolver.resolveCurrentUserId()).thenReturn(1L);
        var page = new PageImpl<>(List.of(testDraft));
        when(draftService.listByDisease(eq(1L), any(Pageable.class))).thenReturn(page);

        mockMvc.perform(get("/api/drafts").param("diseaseId", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));

        verify(draftService).listByDisease(anyLong(), any(Pageable.class));
        verify(draftService, never()).listByUser(anyLong(), any());
    }

    @Test
    void getDraft_shouldReturn200() throws Exception {
        when(draftService.getById(1L)).thenReturn(testDraft);

        mockMvc.perform(get("/api/drafts/{id}", 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.title").value("Test Draft"));
    }

    @Test
    void update_shouldReturn200() throws Exception {
        when(currentUserResolver.resolveCurrentUserId()).thenReturn(1L);
        when(draftService.update(eq(1L), any(UpdateDraftRequest.class), eq(1L))).thenReturn(testDraft);

        var body = objectMapper.writeValueAsString(new UpdateDraftRequest("Updated", List.of()));

        mockMvc.perform(put("/api/drafts/{id}", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Draft updated"));
    }

    @Test
    void delete_shouldReturn200() throws Exception {
        when(currentUserResolver.resolveCurrentUserId()).thenReturn(1L);
        doNothing().when(draftService).delete(1L, 1L);

        mockMvc.perform(delete("/api/drafts/{id}", 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Draft deleted"));
    }

    @Test
    void submit_shouldReturn200() throws Exception {
        when(currentUserResolver.resolveCurrentUserId()).thenReturn(1L);
        when(draftLifecycleService.submitForReview(1L, 1L)).thenReturn(testDraft);

        mockMvc.perform(post("/api/drafts/{id}/submit", 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Draft submitted for review"));
    }

    @Test
    void approve_shouldReturn200() throws Exception {
        when(currentUserResolver.resolveCurrentUserId()).thenReturn(2L);
        when(draftLifecycleService.approve(1L, 2L, "Good")).thenReturn(testDraft);

        var body = objectMapper.writeValueAsString(new DraftReviewRequest("approve", "Good"));

        mockMvc.perform(post("/api/drafts/{id}/approve", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Draft approved"));
    }

    @Test
    void reject_shouldReturn200() throws Exception {
        when(currentUserResolver.resolveCurrentUserId()).thenReturn(2L);
        when(draftLifecycleService.reject(1L, 2L, "Fix it")).thenReturn(testDraft);

        var body = objectMapper.writeValueAsString(new DraftReviewRequest("reject", "Fix it"));

        mockMvc.perform(post("/api/drafts/{id}/reject", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Draft rejected"));
    }

    @Test
    void archive_shouldReturn200() throws Exception {
        when(currentUserResolver.resolveCurrentUserId()).thenReturn(1L);
        when(draftLifecycleService.archive(1L, 1L)).thenReturn(testDraft);

        mockMvc.perform(post("/api/drafts/{id}/archive", 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Draft archived"));
    }

    @Test
    void clone_shouldReturn200() throws Exception {
        when(currentUserResolver.resolveCurrentUserId()).thenReturn(1L);
        when(draftLifecycleService.cloneDraft(1L, 1L)).thenReturn(testDraft);

        mockMvc.perform(post("/api/drafts/{id}/clone", 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Draft cloned"));
    }

    @Test
    void apply_shouldReturn200() throws Exception {
        when(currentUserResolver.resolveCurrentUserId()).thenReturn(1L);
        doNothing().when(draftLifecycleService).applyToDisease(1L, 1L);

        mockMvc.perform(post("/api/drafts/{id}/apply", 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Draft applied to disease"));
    }

    @Test
    void aiGenerate_shouldReturn200() throws Exception {
        when(currentUserResolver.resolveCurrentUserId()).thenReturn(1L);
        when(aiDraftGenerator.generate(any(AiDraftRequest.class), eq(1L))).thenReturn(testDraft);

        var body = objectMapper.writeValueAsString(
                new AiDraftRequest(null, null, "AI Draft", List.of(DraftSectionType.DEFINITION), null, null));

        mockMvc.perform(post("/api/ai/drafts")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("AI draft generation started"));
    }
}
