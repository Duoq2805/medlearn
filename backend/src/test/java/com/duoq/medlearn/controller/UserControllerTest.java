package com.duoq.medlearn.controller;

import com.duoq.medlearn.domain.dto.user.UpdateProfileRequest;
import com.duoq.medlearn.domain.dto.user.UserResponse;
import com.duoq.medlearn.security.JwtService;
import com.duoq.medlearn.security.OAuth2LoginSuccessHandler;
import com.duoq.medlearn.service.UserService;
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

import java.util.Set;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(
    controllers = UserController.class,
    excludeAutoConfiguration = {
        SecurityAutoConfiguration.class,
        SecurityFilterAutoConfiguration.class,
        OAuth2ClientAutoConfiguration.class,
        OAuth2ResourceServerAutoConfiguration.class,
        UserDetailsServiceAutoConfiguration.class
    }
)
class UserControllerTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private ObjectMapper objectMapper;

    @MockBean private UserService userService;
    @MockBean private JwtService jwtService;
    @MockBean private UserDetailsService userDetailsService;
    @MockBean private OAuth2LoginSuccessHandler oAuth2LoginSuccessHandler;
    @MockBean private OAuth2UserService<OAuth2UserRequest, OAuth2User> oAuth2UserService;

    private final UserResponse testUserResponse = UserResponse.builder()
            .id(1L)
            .username("testuser")
            .email("user@test.com")
            .fullName("Test User")
            .roles(Set.of("USER"))
            .isVerified(true)
            .status("ACTIVE")
            .build();

    @Test
    void getMyProfile_shouldReturn200() throws Exception {
        when(userService.getCurrentUser()).thenReturn(testUserResponse);

        mockMvc.perform(get("/api/users/me"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.username").value("testuser"))
                .andExpect(jsonPath("$.data.email").value("user@test.com"));
    }

    @Test
    void getMyProfile_shouldReturnCorrectStructure() throws Exception {
        when(userService.getCurrentUser()).thenReturn(testUserResponse);

        mockMvc.perform(get("/api/users/me"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.id").value(1L))
                .andExpect(jsonPath("$.data.fullName").value("Test User"))
                .andExpect(jsonPath("$.data.isVerified").value(true))
                .andExpect(jsonPath("$.data.status").value("ACTIVE"));
    }

    @Test
    void updateMyProfile_shouldReturn200() throws Exception {
        UpdateProfileRequest request = new UpdateProfileRequest();
        request.setFullName("Updated Name");
        request.setPhoneNumber("1234567890");

        UserResponse updatedDTO = UserResponse.builder()
                .id(1L)
                .username("testuser")
                .email("user@test.com")
                .fullName("Updated Name")
                .roles(Set.of("USER"))
                .isVerified(true)
                .status("ACTIVE")
                .build();

        when(userService.updateProfile(any())).thenReturn(updatedDTO);

        mockMvc.perform(put("/api/users/me")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.fullName").value("Updated Name"))
                .andExpect(jsonPath("$.message").value("Profile updated"));
    }

    @Test
    void updateMyProfile_shouldReturn400_whenInvalidEmail() throws Exception {
        UpdateProfileRequest request = new UpdateProfileRequest();
        request.setEmail("not-an-email");

        mockMvc.perform(put("/api/users/me")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void updateMyProfile_shouldReturn400_whenUsernameTooShort() throws Exception {
        UpdateProfileRequest request = new UpdateProfileRequest();
        request.setUsername("ab");

        mockMvc.perform(put("/api/users/me")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }
}
