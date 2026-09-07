package com.duoq.medlearn.controller;
import com.duoq.medlearn.knowledge.symptom.controller.SymptomController;

import com.duoq.medlearn.knowledge.symptom.dto.request.CreateSymptomRequest;
import com.duoq.medlearn.knowledge.symptom.dto.response.SymptomResponse;
import com.duoq.medlearn.knowledge.symptom.dto.request.UpdateSymptomRequest;
import com.duoq.medlearn.common.exception.ResourceNotFoundException;
import com.duoq.medlearn.common.security.JwtService;
import com.duoq.medlearn.auth.security.OAuth2LoginSuccessHandler;
import com.duoq.medlearn.knowledge.symptom.service.SymptomCheckerService;
import com.duoq.medlearn.knowledge.symptom.service.SymptomService;
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

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(
    controllers = SymptomController.class,
    excludeAutoConfiguration = {
        SecurityAutoConfiguration.class,
        SecurityFilterAutoConfiguration.class,
        OAuth2ClientAutoConfiguration.class,
        OAuth2ResourceServerAutoConfiguration.class,
        UserDetailsServiceAutoConfiguration.class
    }
)
class SymptomControllerTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private ObjectMapper objectMapper;

    @MockBean private SymptomService symptomService;
    @MockBean private SymptomCheckerService symptomCheckerService;

    // Security bean mocks 閳?required because @WebMvcTest excludes @Service beans
    // but SecurityConfig (@Configuration) + JwtAuthenticationFilter (@Component)
    // are still discovered via component scanning.
    @MockBean private JwtService jwtService;
    @MockBean private UserDetailsService userDetailsService;
    @MockBean private OAuth2LoginSuccessHandler oAuth2LoginSuccessHandler;
    @MockBean private OAuth2UserService<OAuth2UserRequest, OAuth2User> oAuth2UserService;

    private final SymptomResponse testSymptom = new SymptomResponse(
            1L, "Fever", "fever", "Elevated body temperature",
            OffsetDateTime.now(), OffsetDateTime.now()
    );

    @Test
    void getAllSymptoms_shouldReturn200() throws Exception {
        when(symptomService.getAllSymptoms()).thenReturn(List.of(testSymptom));

        mockMvc.perform(get("/api/symptoms"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[0].name").value("Fever"));
    }

    @Test
    void getAllSymptoms_shouldReturnEmptyList() throws Exception {
        when(symptomService.getAllSymptoms()).thenReturn(List.of());

        mockMvc.perform(get("/api/symptoms"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").isEmpty());
    }

    @Test
    void getSymptomById_shouldReturn200() throws Exception {
        when(symptomService.getSymptomById(1L)).thenReturn(testSymptom);

        mockMvc.perform(get("/api/symptoms/{id}", 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.name").value("Fever"));
    }

    @Test
    void getSymptomById_shouldReturn404_whenNotFound() throws Exception {
        when(symptomService.getSymptomById(999L)).thenThrow(new ResourceNotFoundException("Symptom not found"));

        mockMvc.perform(get("/api/symptoms/{id}", 999L))
                .andExpect(status().isNotFound());
    }

    @Test
    void searchSymptoms_shouldReturn200() throws Exception {
        when(symptomService.searchSymptoms("fever")).thenReturn(List.of(testSymptom));

        mockMvc.perform(get("/api/symptoms/search").param("keyword", "fever"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[0].name").value("Fever"));
    }

    @Test
    void searchSymptoms_shouldReturn400_whenKeywordTooShort() throws Exception {
        mockMvc.perform(get("/api/symptoms/search").param("keyword", "a"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void createSymptom_shouldReturn200() throws Exception {
        CreateSymptomRequest request = new CreateSymptomRequest();
        request.setName("Fever");
        request.setDescription("Elevated body temperature");

        when(symptomService.createSymptom(any())).thenReturn(testSymptom);

        mockMvc.perform(post("/api/symptoms")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.name").value("Fever"));
    }

    @Test
    void createSymptom_shouldReturn400_whenNameExists() throws Exception {
        CreateSymptomRequest request = new CreateSymptomRequest();
        request.setName("Fever");

        when(symptomService.createSymptom(any()))
                .thenThrow(new IllegalStateException("Symptom name already exists"));

        mockMvc.perform(post("/api/symptoms")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void createSymptom_shouldReturn400_whenNameBlank() throws Exception {
        CreateSymptomRequest request = new CreateSymptomRequest();
        request.setName("");

        mockMvc.perform(post("/api/symptoms")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().is4xxClientError());
    }

    @Test
    void updateSymptom_shouldReturn200() throws Exception {
        UpdateSymptomRequest request = new UpdateSymptomRequest();
        request.setName("High Fever");

        when(symptomService.updateSymptom(eq(1L), any())).thenReturn(testSymptom);

        mockMvc.perform(put("/api/symptoms/{id}", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Symptom updated successfully"));
    }

    @Test
    void updateSymptom_shouldReturn404_whenNotFound() throws Exception {
        UpdateSymptomRequest request = new UpdateSymptomRequest();
        request.setName("High Fever");

        when(symptomService.updateSymptom(eq(999L), any()))
                .thenThrow(new ResourceNotFoundException("Symptom not found"));

        mockMvc.perform(put("/api/symptoms/{id}", 999L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound());
    }

    @Test
    void updateSymptom_shouldReturn400_whenNameExists() throws Exception {
        UpdateSymptomRequest request = new UpdateSymptomRequest();
        request.setName("Cough");

        when(symptomService.updateSymptom(eq(1L), any()))
                .thenThrow(new IllegalStateException("Symptom name already exists"));

        mockMvc.perform(put("/api/symptoms/{id}", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void deleteSymptom_shouldReturn200() throws Exception {
        mockMvc.perform(delete("/api/symptoms/{id}", 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Symptom deleted successfully"));

        verify(symptomService).deleteSymptom(1L);
    }

    @Test
    void deleteSymptom_shouldReturn404_whenNotFound() throws Exception {
        doThrow(new ResourceNotFoundException("Symptom not found"))
                .when(symptomService).deleteSymptom(999L);

        mockMvc.perform(delete("/api/symptoms/{id}", 999L))
                .andExpect(status().isNotFound());
    }
}
