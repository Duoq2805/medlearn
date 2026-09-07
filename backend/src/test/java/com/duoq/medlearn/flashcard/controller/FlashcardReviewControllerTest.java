package com.duoq.medlearn.flashcard.controller;

import com.duoq.medlearn.flashcard.dto.response.FlashcardReviewResponse;
import com.duoq.medlearn.flashcard.service.FlashcardReviewService;
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
import org.springframework.http.MediaType;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(
    controllers = FlashcardReviewController.class,
    excludeAutoConfiguration = {
        SecurityAutoConfiguration.class,
        SecurityFilterAutoConfiguration.class,
        OAuth2ClientAutoConfiguration.class,
        OAuth2ResourceServerAutoConfiguration.class,
        UserDetailsServiceAutoConfiguration.class
    }
)
class FlashcardReviewControllerTest {

    @Autowired private MockMvc mockMvc;
    @MockBean private FlashcardReviewService reviewService;
    @MockBean private CurrentUserResolver currentUserResolver;
    @MockBean private JwtService jwtService;
    @MockBean private UserDetailsService userDetailsService;
    @MockBean private OAuth2LoginSuccessHandler oAuth2LoginSuccessHandler;

    @Test
    void getDue_ShouldReturn200() throws Exception {
        when(currentUserResolver.resolveCurrentUserId()).thenReturn(1L);
        when(reviewService.getDueCards(1L, 20)).thenReturn(List.of());

        mockMvc.perform(get("/api/flashcards/due"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    void review_ShouldReturn200() throws Exception {
        when(currentUserResolver.resolveCurrentUserId()).thenReturn(1L);
        when(reviewService.review(anyLong(), anyInt(), any(), anyLong()))
                .thenReturn(FlashcardReviewResponse.builder()
                        .flashcardId(1L).quality(4).interval(1).mastered(false).build());

        mockMvc.perform(post("/api/flashcards/1/review")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"quality\": 4}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.quality").value(4));
    }

    @Test
    void review_InvalidQuality_ShouldReturn400() throws Exception {
        mockMvc.perform(post("/api/flashcards/1/review")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"quality\": 6}"))
                .andExpect(status().isBadRequest());
    }
}
