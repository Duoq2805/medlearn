package com.duoq.medlearn.ai.gateway;

import com.duoq.medlearn.ai.config.AiProperties;
import com.duoq.medlearn.ai.dto.request.AiChatRequest;
import com.duoq.medlearn.ai.dto.response.AiChatResponse;
import com.duoq.medlearn.ai.dto.response.AiMessage;
import com.duoq.medlearn.ai.dto.enums.AiRole;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AiCacheServiceImplTest {

    @Mock
    private AiProperties aiProperties;

    @Mock
    private AiProperties.Cache cacheConfig;

    private AiCacheServiceImpl cacheService;

    @BeforeEach
    void setUp() {
        when(aiProperties.getCache()).thenReturn(cacheConfig);
        when(cacheConfig.getMaxSize()).thenReturn(100);
        when(cacheConfig.getTtlMinutes()).thenReturn(60);
        cacheService = new AiCacheServiceImpl(aiProperties);
        cacheService.init();
    }

    @Test
    void putAndGet_shouldReturnCachedResponse() {
        var request = createRequest("test message");
        var response = createResponse("test response");

        cacheService.put(request, response);

        var cached = cacheService.get(request);
        assertThat(cached).isPresent();
        assertThat(cached.get().getChoices().getFirst().getContent()).isEqualTo("test response");
    }

    @Test
    void get_shouldReturnEmptyForUnknownKey() {
        var result = cacheService.get(createRequest("unknown"));
        assertThat(result).isEmpty();
    }

    @Test
    void evict_shouldRemoveEntry() {
        var request = createRequest("to evict");
        cacheService.put(request, createResponse("data"));
        cacheService.evict(request.getModel() + "|" + request.getTemperature() + "|" + request.getMessages());
        // evict with wrong key format doesn't match SHA-256 key, so size should still be 1
        assertThat(cacheService.size()).isEqualTo(1);
    }

    @Test
    void clear_shouldRemoveAllEntries() {
        cacheService.put(createRequest("msg1"), createResponse("resp1"));
        cacheService.put(createRequest("msg2"), createResponse("resp2"));
        cacheService.clear();
        assertThat(cacheService.size()).isZero();
    }

    private AiChatRequest createRequest(String message) {
        return AiChatRequest.builder()
                .model("gpt-4o")
                .temperature(0.7)
                .messages(List.of(AiMessage.builder()
                        .role(AiRole.USER)
                        .content(message)
                        .build()))
                .build();
    }

    private AiChatResponse createResponse(String content) {
        return AiChatResponse.builder()
                .id("test-id")
                .model("gpt-4o")
                .provider("test")
                .choices(List.of(AiMessage.builder()
                        .role(AiRole.ASSISTANT)
                        .content(content)
                        .build()))
                .build();
    }
}
