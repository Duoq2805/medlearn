package com.duoq.medlearn.flashcard.controller;

import com.duoq.medlearn.flashcard.dto.request.FlashcardGenerateRequest;
import com.duoq.medlearn.flashcard.dto.response.FlashcardGenerateResponse;
import com.duoq.medlearn.flashcard.dto.response.FlashcardResponse;
import com.duoq.medlearn.flashcard.dto.response.FlashcardStatsResponse;
import com.duoq.medlearn.flashcard.enums.FlashcardDifficulty;
import com.duoq.medlearn.flashcard.service.FlashcardGenerator;
import com.duoq.medlearn.flashcard.service.FlashcardService;
import com.duoq.medlearn.common.security.CurrentUserResolver;
import com.duoq.medlearn.common.security.JwtService;
import com.duoq.medlearn.auth.security.OAuth2LoginSuccessHandler;
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
import org.springframework.http.MediaType;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserService;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(
    controllers = FlashcardController.class,
    excludeAutoConfiguration = {
        SecurityAutoConfiguration.class,
        SecurityFilterAutoConfiguration.class,
        OAuth2ClientAutoConfiguration.class,
        OAuth2ResourceServerAutoConfiguration.class,
        UserDetailsServiceAutoConfiguration.class
    }
)
class FlashcardControllerTest {

    @Autowired private MockMvc mockMvc;
    @MockBean private FlashcardGenerator flashcardGenerator;
    @MockBean private FlashcardService flashcardService;
    @MockBean private CurrentUserResolver currentUserResolver;
    @MockBean private JwtService jwtService;
    @MockBean private UserDetailsService userDetailsService;
    @MockBean private OAuth2LoginSuccessHandler oAuth2LoginSuccessHandler;

    @Test
    void generate_ShouldReturn201() throws Exception {
        when(currentUserResolver.resolveCurrentUserId()).thenReturn(1L);
        when(flashcardGenerator.generate(any(), anyLong())).thenReturn(
                FlashcardGenerateResponse.builder()
                        .generationId("gen-1").totalGenerated(2)
                        .flashcards(List.of())
                        .build());

        mockMvc.perform(post("/api/flashcards/generate")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"title": "Test", "diseaseId": 1, "count": 2}
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.totalGenerated").value(2));
    }

    @Test
    void generate_EmptyTitle_ShouldReturn400() throws Exception {
        mockMvc.perform(post("/api/flashcards/generate")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"title": "", "diseaseId": 1}
                                """))
                .andExpect(status().isBadRequest());
    }

    @Test
    void getById_ShouldReturn200() throws Exception {
        when(flashcardService.getById(1L)).thenReturn(
                FlashcardResponse.builder().id(1L).question("Q?").answer("A").build());

        mockMvc.perform(get("/api/flashcards/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.question").value("Q?"));
    }

    @Test
    void listByUser_ShouldReturn200() throws Exception {
        when(currentUserResolver.resolveCurrentUserId()).thenReturn(1L);
        when(flashcardService.listByUser(anyLong(), any())).thenReturn(new PageImpl<>(List.of()));

        mockMvc.perform(get("/api/flashcards?page=0&size=20"))
                .andExpect(status().isOk());
    }

    @Test
    void delete_ShouldReturn200() throws Exception {
        when(currentUserResolver.resolveCurrentUserId()).thenReturn(1L);
        mockMvc.perform(delete("/api/flashcards/1").with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    void stats_ShouldReturn200() throws Exception {
        when(currentUserResolver.resolveCurrentUserId()).thenReturn(1L);
        when(flashcardService.getStats(1L)).thenReturn(
                FlashcardStatsResponse.builder().totalCards(100L).totalDecks(5L).build());

        mockMvc.perform(get("/api/flashcards/stats"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.totalCards").value(100));
    }
}
