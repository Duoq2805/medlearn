package com.duoq.medlearn.ai.controller;

import com.duoq.medlearn.ai.dto.response.AiMessage;
import com.duoq.medlearn.ai.gateway.AiGatewayRouter;
import com.duoq.medlearn.ai.prompt.PromptBuilder;
import com.duoq.medlearn.ai.prompt.PromptTemplateService;
import com.duoq.medlearn.ai.prompt.PromptTester;
import com.duoq.medlearn.ai.prompt.PromptValidator;
import com.duoq.medlearn.ai.prompt.VariablesExtractor;
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

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(
    controllers = AiAdminController.class,
    excludeAutoConfiguration = {
        SecurityAutoConfiguration.class,
        SecurityFilterAutoConfiguration.class,
        OAuth2ClientAutoConfiguration.class,
        OAuth2ResourceServerAutoConfiguration.class,
        UserDetailsServiceAutoConfiguration.class
    }
)
class AiAdminControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private PromptTemplateService promptTemplateService;

    @MockBean
    private PromptBuilder promptBuilder;

    @MockBean
    private PromptValidator promptValidator;

    @MockBean
    private VariablesExtractor variablesExtractor;

    @MockBean
    private PromptTester promptTester;

    @MockBean
    private AiGatewayRouter gatewayRouter;

    @MockBean
    private UserDetailsService userDetailsService;

    @MockBean
    @SuppressWarnings("unchecked")
    private OAuth2UserService<OAuth2UserRequest, OAuth2User> oAuth2UserService;

    @MockBean
    private JwtService jwtService;

    @MockBean
    private OAuth2LoginSuccessHandler oAuth2LoginSuccessHandler;

    @Test
    void listPrompts_shouldReturn200() throws Exception {
        when(promptTemplateService.listAll()).thenReturn(List.of());
        mockMvc.perform(get("/api/ai/admin/prompts"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    void extractVariables_shouldReturnVariables() throws Exception {
        when(variablesExtractor.extract("Hello {{name}}")).thenReturn(java.util.Set.of("name"));

        mockMvc.perform(post("/api/ai/admin/prompts/extract-variables")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"template\":\"Hello {{name}}\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.variables[0]").value("name"));
    }

    @Test
    void validatePrompt_shouldReturnOkWhenValid() throws Exception {
        when(promptValidator.validate("sys", "user")).thenReturn(List.of());

        mockMvc.perform(post("/api/ai/admin/prompts/validate")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"systemPrompt\":\"sys\",\"userPromptTemplate\":\"user\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    void buildPrompt_shouldReturnMessages() throws Exception {
        when(promptBuilder.build(any(), any())).thenReturn(List.of(
                AiMessage.builder().content("Hello").build()
        ));

        mockMvc.perform(post("/api/ai/admin/prompts/build")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"code\":\"test\",\"variables\":{\"name\":\"world\"}}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[0].content").value("Hello"));
    }
}
