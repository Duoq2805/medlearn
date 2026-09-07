package com.duoq.medlearn.ai.gateway;

import com.duoq.medlearn.ai.config.AiProperties;
import com.duoq.medlearn.ai.dto.request.AiChatRequest;
import com.duoq.medlearn.ai.dto.response.AiChatResponse;
import com.duoq.medlearn.ai.dto.response.AiMessage;
import com.duoq.medlearn.ai.enums.AiRole;
import com.duoq.medlearn.ai.exception.AiConfigurationException;
import com.duoq.medlearn.ai.gateway.provider.AiProvider;
import com.duoq.medlearn.ai.model.AiModel;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AiGatewayRouterTest {

    @Mock private AiGateway nineRouterGateway;
    @Mock private AiProvider mockProvider;       // kept to verify AiProvider is NOT used in chat()
    @Mock private AiProperties aiProperties;
    @Mock private AiProperties.Gateway gatewayConfig;

    private AiGatewayRouter router;

    private AiChatRequest sampleRequest() {
        return AiChatRequest.builder()
                .messages(List.of(AiMessage.builder().role(AiRole.USER).content("test").build()))
                .model(AiModel.GPT_5_MINI.getModelId())
                .build();
    }

    @BeforeEach
    void setUp() {
        router = new AiGatewayRouter(List.of(nineRouterGateway), List.of(mockProvider), aiProperties);
    }

    // ── resolve() tests ────────────────────────────────────────────────────────

    @Test
    void resolve_namedProvider_returnsMatchingGateway() {
        when(nineRouterGateway.getProviderName()).thenReturn("nine-router");

        var gateway = router.resolve("nine-router");

        assertThat(gateway.getProviderName()).isEqualTo("nine-router");
    }

    @Test
    void resolve_unknownProvider_throwsAiConfigurationException() {
        when(nineRouterGateway.getProviderName()).thenReturn("nine-router");

        assertThatThrownBy(() -> router.resolve("unknown-provider"))
                .isInstanceOf(AiConfigurationException.class)
                .hasMessageContaining("unknown-provider");
    }

    @Test
    void resolve_nullProvider_returnsDefaultGateway() {
        when(nineRouterGateway.getProviderName()).thenReturn("nine-router");
        when(aiProperties.getGateway()).thenReturn(gatewayConfig);
        when(gatewayConfig.getDefaultProvider()).thenReturn("nine-router");

        var gateway = router.resolve(null);

        assertThat(gateway.getProviderName()).isEqualTo("nine-router");
    }

    @Test
    void resolve_nullProvider_noDefaultConfigured_throwsAiConfigurationException() {
        // gatewayList has "nine-router" but defaultProvider is set to something else
        when(nineRouterGateway.getProviderName()).thenReturn("nine-router");
        when(aiProperties.getGateway()).thenReturn(gatewayConfig);
        when(gatewayConfig.getDefaultProvider()).thenReturn("missing-provider");

        assertThatThrownBy(() -> router.resolve(null))
                .isInstanceOf(AiConfigurationException.class)
                .hasMessageContaining("No default gateway configured");
    }

    // ── chat() routing tests ───────────────────────────────────────────────────

    @Test
    void chat_delegatesToDefaultGateway_notAiProvider() {
        // Arrange
        when(nineRouterGateway.getProviderName()).thenReturn("nine-router");
        when(aiProperties.getGateway()).thenReturn(gatewayConfig);
        when(gatewayConfig.getDefaultProvider()).thenReturn("nine-router");

        var request = sampleRequest();
        var expected = AiChatResponse.builder().id("resp-1").model("gpt-5-mini").provider("nine-router").build();
        when(nineRouterGateway.chat(request)).thenReturn(expected);

        // Act
        var result = router.chat(request, AiModel.GPT_5_MINI);

        // Assert: NineRouterGateway.chat(request) was called
        verify(nineRouterGateway).chat(request);
        // Assert: AiProvider was NEVER consulted
        verify(mockProvider, never()).chat(any(), any());
        verify(mockProvider, never()).supportsModel(any());

        assertThat(result.getId()).isEqualTo("resp-1");
        assertThat(result.getProvider()).isEqualTo("nine-router");
    }

    @Test
    void chat_differentModels_allRouteToSameGateway() {
        // 9router is a unified gateway: GPT_5, CLAUDE_4_SONNET, GEMINI_2_5_PRO all go through it
        when(nineRouterGateway.getProviderName()).thenReturn("nine-router");
        when(aiProperties.getGateway()).thenReturn(gatewayConfig);
        when(gatewayConfig.getDefaultProvider()).thenReturn("nine-router");
        when(nineRouterGateway.chat(any())).thenReturn(
                AiChatResponse.builder().id("r").provider("nine-router").build());

        for (AiModel model : List.of(AiModel.GPT_5, AiModel.GPT_5_MINI, AiModel.CLAUDE_4_SONNET,
                AiModel.GEMINI_2_5_PRO, AiModel.DEEPSEEK_CHAT)) {
            router.chat(sampleRequest(), model);
        }

        // All 5 calls routed to nineRouterGateway, never to AiProvider
        verify(nineRouterGateway, times(5)).chat(any());
        verify(mockProvider, never()).supportsModel(any());
    }

    @Test
    void chat_providerListEmpty_stillWorks() {
        // router with empty AiProvider list — must not throw
        var routerNoProviders = new AiGatewayRouter(List.of(nineRouterGateway), List.of(), aiProperties);
        when(nineRouterGateway.getProviderName()).thenReturn("nine-router");
        when(aiProperties.getGateway()).thenReturn(gatewayConfig);
        when(gatewayConfig.getDefaultProvider()).thenReturn("nine-router");
        when(nineRouterGateway.chat(any())).thenReturn(
                AiChatResponse.builder().id("ok").build());

        var result = routerNoProviders.chat(sampleRequest(), AiModel.GPT_5_MINI);

        assertThat(result.getId()).isEqualTo("ok");
    }

    // ── chatStream() routing tests ─────────────────────────────────────────────

    @Test
    void chatStream_delegatesToDefaultGateway() {
        when(nineRouterGateway.getProviderName()).thenReturn("nine-router");
        when(aiProperties.getGateway()).thenReturn(gatewayConfig);
        when(gatewayConfig.getDefaultProvider()).thenReturn("nine-router");

        var onCompleteCalled = new AtomicBoolean(false);
        doAnswer(inv -> { inv.<Runnable>getArgument(2).run(); return null; })
                .when(nineRouterGateway).chatStream(any(), any(), any(), any());

        router.chatStream(sampleRequest(), AiModel.GPT_5_MINI,
                chunk -> {}, () -> onCompleteCalled.set(true), err -> {});

        verify(nineRouterGateway).chatStream(any(), any(), any(), any());
        verify(mockProvider, never()).chatStream(any(), any(), any(), any(), any());
        assertThat(onCompleteCalled.get()).isTrue();
    }

    // ── resolveProvider() tests — kept as documentation of dead-code boundary ──

    @Test
    void resolveProvider_withMockImpl_works() {
        // resolveProvider() itself is not broken; it just has no real impl in prod
        when(mockProvider.supportsModel(AiModel.GPT_5)).thenReturn(true);

        var provider = router.resolveProvider(AiModel.GPT_5);

        assertThat(provider).isSameAs(mockProvider);
    }

    @Test
    void resolveProvider_noMatchingProvider_throwsAiConfigurationException() {
        // This is the exact bug that was triggered in production when providers list is empty
        var routerNoProviders = new AiGatewayRouter(List.of(nineRouterGateway), List.of(), aiProperties);

        assertThatThrownBy(() -> routerNoProviders.resolveProvider(AiModel.GPT_5_MINI))
                .isInstanceOf(AiConfigurationException.class)
                .hasMessageContaining("No provider found for model");
    }

    // ── listModels() tests ─────────────────────────────────────────────────────

    @Test
    void listModels_delegatesToNamedGateway() {
        when(nineRouterGateway.getProviderName()).thenReturn("nine-router");
        when(nineRouterGateway.listModels()).thenReturn(List.of());

        var models = router.listModels("nine-router");

        verify(nineRouterGateway).listModels();
        assertThat(models).isEmpty();
    }

    @Test
    void listAllModels_aggregatesAllGateways() {
        when(nineRouterGateway.getProviderName()).thenReturn("nine-router");
        when(nineRouterGateway.listModels()).thenReturn(List.of());

        var all = router.listAllModels();

        assertThat(all).containsKey("nine-router");
    }
}
