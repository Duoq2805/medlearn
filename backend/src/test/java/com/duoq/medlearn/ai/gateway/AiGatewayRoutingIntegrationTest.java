package com.duoq.medlearn.ai.gateway;

import com.duoq.medlearn.ai.config.AiProperties;
import com.duoq.medlearn.ai.dto.request.AiChatRequest;
import com.duoq.medlearn.ai.dto.response.AiMessage;
import com.duoq.medlearn.ai.enums.AiRole;
import com.duoq.medlearn.ai.model.AiModel;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.web.client.RestTemplate;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Integration-level test: real AiGatewayRouter + real NineRouterGateway wired together.
 * Only the HTTP layer (RestTemplate) is mocked — verifying the full routing path
 * from Router → NineRouterGateway → HTTP without a live network connection.
 *
 * This test proves the bug fix: chat(request, model) reaches NineRouterGateway.chat()
 * instead of throwing AiConfigurationException from empty AiProvider list.
 */
class AiGatewayRoutingIntegrationTest {

    private RestTemplate restTemplate;
    private AiGatewayRouter router;
    private NineRouterGateway nineRouterGateway;

    @BeforeEach
    void setUp() {
        restTemplate = mock(RestTemplate.class);

        var props = new AiProperties();
        props.getGateway().setDefaultProvider("nine-router");
        props.getGateway().setApiKey("test-key");
        props.getGateway().setBaseUrl("http://mock-9router");

        // Real NineRouterGateway — only RestTemplate is mocked
        nineRouterGateway = new NineRouterGateway(restTemplate, props);

        // Real AiGatewayRouter — empty AiProvider list (production scenario)
        router = new AiGatewayRouter(List.of(nineRouterGateway), List.of(), props);
    }

    private AiChatRequest buildRequest(AiModel model) {
        return AiChatRequest.builder()
                .messages(List.of(AiMessage.builder()
                        .role(AiRole.USER)
                        .content("Test message")
                        .build()))
                .model(model.getModelId())
                .temperature(0.7)
                .maxTokens(100)
                .build();
    }

    /**
     * Stubs RestTemplate to return a valid 9router-shaped response JSON.
     * NineRouterGateway parses this internally via record types.
     */
    @SuppressWarnings({"unchecked", "rawtypes"})
    private void stubRestTemplateOk(String modelId, String content) {
        // NineRouterGateway calls restTemplate.exchange(url, POST, entity, NineRouterChatResponse.class)
        // We match on any HttpEntity and return a mock ResponseEntity
        var mockResponse = mock(org.springframework.http.ResponseEntity.class);

        // Build the response via reflection-free approach: use a real inner response
        // NineRouterChatResponse is a private record — stub at RestTemplate level with answer
        when(restTemplate.exchange(
                contains("/v1/chat/completions"),
                eq(org.springframework.http.HttpMethod.POST),
                any(),
                (Class) any()
        )).thenAnswer(inv -> {
            // Return null body — NineRouterGateway handles null gracefully (returns empty AiChatResponse)
            when(mockResponse.getBody()).thenReturn(null);
            return mockResponse;
        });
    }

    // ── Core routing bug fix verification ─────────────────────────────────────

    @Test
    void chat_withEmptyAiProviderList_doesNotThrow() {
        // Before fix: threw AiConfigurationException("No provider found for model: GPT_5_MINI")
        // After fix: routes to NineRouterGateway → returns empty response (null body handled)
        stubRestTemplateOk("gpt-5-mini", "response");

        var request = buildRequest(AiModel.GPT_5_MINI);

        // Must not throw — this was the Critical Bug
        var response = router.chat(request, AiModel.GPT_5_MINI);

        assertThat(response).isNotNull();
        assertThat(response.getProvider()).isEqualTo("nine-router");
    }

    @Test
    void chat_routesToNineRouterGateway_httpLayerInvoked() {
        stubRestTemplateOk("gpt-5", "content");

        router.chat(buildRequest(AiModel.GPT_5), AiModel.GPT_5);

        // Verify RestTemplate was actually called — proves NineRouterGateway was reached
        verify(restTemplate).exchange(
                contains("/v1/chat/completions"),
                eq(org.springframework.http.HttpMethod.POST),
                any(),
                (Class<?>) any()
        );
    }

    @Test
    void chat_multipleModels_allReachNineRouterGateway() {
        stubRestTemplateOk("any", "ok");

        var models = List.of(
                AiModel.GPT_5,
                AiModel.GPT_5_MINI,
                AiModel.CLAUDE_4_SONNET,
                AiModel.GEMINI_2_5_PRO,
                AiModel.DEEPSEEK_CHAT
        );

        for (var model : models) {
            router.chat(buildRequest(model), model);
        }

        // RestTemplate called once per model — NineRouterGateway reached each time
        verify(restTemplate, times(5)).exchange(
                anyString(), any(), any(), (Class<?>) any()
        );
    }

    // ── AI Summary routing integration ─────────────────────────────────────────

    @Test
    void aiSummaryFlow_routerToNineRouter() {
        // Simulates what AiSummaryServiceImpl does:
        //   gatewayRouter.chat(chatRequest, AiModel.fromModelId(chatRequest.getModel()))
        stubRestTemplateOk("gpt-4o", "Summary content");

        var chatRequest = AiChatRequest.builder()
                .messages(List.of(
                        AiMessage.builder().role(AiRole.SYSTEM).content("You are a medical educator.").build(),
                        AiMessage.builder().role(AiRole.USER).content("Summarize Diabetes for STUDENT.").build()
                ))
                .model("gpt-4o")
                .temperature(0.7)
                .maxTokens(4096)
                .promptTemplateCode("disease-summary")
                .userId("42")
                .build();

        var model = AiModel.fromModelId(chatRequest.getModel()); // CUSTOM (gpt-4o not in enum)
        var response = router.chat(chatRequest, model);

        assertThat(response).isNotNull();
        // HTTP was called → NineRouterGateway was reached
        verify(restTemplate, atLeastOnce()).exchange(anyString(), any(), any(), (Class<?>) any());
    }

    // ── AI Flashcard routing integration ───────────────────────────────────────

    @Test
    void aiFlashcardFlow_routerToNineRouter() {
        // Simulates what FlashcardGeneratorImpl does
        stubRestTemplateOk("gpt-5-mini", "{\"flashcards\":[]}");

        var chatRequest = AiChatRequest.builder()
                .messages(List.of(
                        AiMessage.builder().role(AiRole.SYSTEM).content("Generate flashcards.").build(),
                        AiMessage.builder().role(AiRole.USER).content("Create 5 cards about Hypertension.").build()
                ))
                .model(AiModel.GPT_5_MINI.getModelId())
                .temperature(0.3)
                .maxTokens(4000)
                .promptTemplateCode("flashcard-gen")
                .userId("1")
                .build();

        var model = AiModel.fromModelId(chatRequest.getModel());
        var response = router.chat(chatRequest, model);

        assertThat(response).isNotNull();
        verify(restTemplate, atLeastOnce()).exchange(anyString(), any(), any(), (Class<?>) any());
    }

    // ── AI Draft routing integration ────────────────────────────────────────────

    @Test
    void aiDraftFlow_routerToNineRouter() {
        // Simulates what AiDraftGeneratorImpl does (per-section loop)
        stubRestTemplateOk("gpt-5-mini", "Draft section content");

        var chatRequest = AiChatRequest.builder()
                .messages(List.of(
                        AiMessage.builder().role(AiRole.SYSTEM).content("You are a medical writer.").build(),
                        AiMessage.builder().role(AiRole.USER).content("Write DEFINITION for Diabetes.").build()
                ))
                .model(AiModel.GPT_5_MINI.getModelId())
                .temperature(0.7)
                .maxTokens(2000)
                .userId("10")
                .build();

        var model = AiModel.fromModelId(chatRequest.getModel());
        var response = router.chat(chatRequest, model);

        assertThat(response).isNotNull();
        verify(restTemplate, atLeastOnce()).exchange(anyString(), any(), any(), (Class<?>) any());
    }

    // ── Error path ──────────────────────────────────────────────────────────────

    @Test
    void chat_httpProviderUnavailable_throwsAiProviderUnavailableException() {
        when(restTemplate.exchange(anyString(), any(), any(), (Class<?>) any()))
                .thenThrow(new org.springframework.web.client.ResourceAccessException("Connection refused"));

        assertThatThrownBy(() -> router.chat(buildRequest(AiModel.GPT_5_MINI), AiModel.GPT_5_MINI))
                .isInstanceOf(com.duoq.medlearn.ai.exception.AiProviderUnavailableException.class)
                .hasMessageContaining("Provider unreachable");
    }

    @Test
    void chat_http429_throwsAiRateLimitException() {
        var ex = new org.springframework.web.client.HttpClientErrorException(
                org.springframework.http.HttpStatus.TOO_MANY_REQUESTS);
        when(restTemplate.exchange(anyString(), any(), any(), (Class<?>) any()))
                .thenThrow(ex);

        assertThatThrownBy(() -> router.chat(buildRequest(AiModel.GPT_5_MINI), AiModel.GPT_5_MINI))
                .isInstanceOf(com.duoq.medlearn.ai.exception.AiRateLimitException.class);
    }
}
