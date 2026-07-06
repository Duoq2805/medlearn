package com.duoq.medlearn.ai.gateway;

import com.duoq.medlearn.ai.config.AiProperties;
import com.duoq.medlearn.ai.dto.request.AiChatRequest;
import com.duoq.medlearn.ai.dto.response.AiChatResponse;
import com.duoq.medlearn.ai.dto.response.AiMessage;
import com.duoq.medlearn.ai.dto.enums.AiRole;
import com.duoq.medlearn.ai.exception.AiConfigurationException;
import com.duoq.medlearn.ai.gateway.provider.AiProvider;
import com.duoq.medlearn.ai.model.AiModel;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AiGatewayRouterTest {

    @Mock
    private AiGateway nineRouterGateway;

    @Mock
    private AiProvider mockProvider;

    @Mock
    private AiProperties aiProperties;

    @Mock
    private AiProperties.Gateway gatewayConfig;

    private AiGatewayRouter router;

    @BeforeEach
    void setUp() {
        router = new AiGatewayRouter(List.of(nineRouterGateway), List.of(mockProvider), aiProperties);
    }

    @Test
    void resolve_shouldReturnMatchingGateway() {
        when(nineRouterGateway.getProviderName()).thenReturn("nine-router");
        var gateway = router.resolve("nine-router");
        assertThat(gateway.getProviderName()).isEqualTo("nine-router");
    }

    @Test
    void resolve_shouldThrowForUnknownProvider() {
        when(nineRouterGateway.getProviderName()).thenReturn("nine-router");
        assertThatThrownBy(() -> router.resolve("unknown"))
                .isInstanceOf(AiConfigurationException.class);
    }

    @Test
    void resolve_shouldReturnDefaultWhenNull() {
        when(nineRouterGateway.getProviderName()).thenReturn("nine-router");
        when(aiProperties.getGateway()).thenReturn(gatewayConfig);
        when(gatewayConfig.getDefaultProvider()).thenReturn("nine-router");
        var gateway = router.resolve(null);
        assertThat(gateway.getProviderName()).isEqualTo("nine-router");
    }

    @Test
    void chat_shouldDelegateToAiProvider() {
        var request = AiChatRequest.builder()
                .messages(List.of(AiMessage.builder().role(AiRole.USER).content("hi").build()))
                .build();
        var response = AiChatResponse.builder().id("test").build();
        when(mockProvider.supportsModel(any())).thenReturn(true);
        when(mockProvider.chat(request, AiModel.GPT_5)).thenReturn(response);

        var result = router.chat(request, AiModel.GPT_5);
        assertThat(result.getId()).isEqualTo("test");
    }
}
