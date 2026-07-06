package com.duoq.medlearn.ai.usage;

import com.duoq.medlearn.ai.repository.AiUsageLogRepository;
import com.duoq.medlearn.ai.usage.entity.AiUsageLog;
import com.duoq.medlearn.domain.entity.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.OffsetDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AiUsageServiceImplTest {

    @Mock
    private AiUsageLogRepository repository;

    @Captor
    private ArgumentCaptor<AiUsageLog> logCaptor;

    private AiUsageServiceImpl usageService;

    @BeforeEach
    void setUp() {
        usageService = new AiUsageServiceImpl(repository);
    }

    @Test
    void log_shouldSaveUsageEntry() {
        var user = User.builder().id(1L).build();
        usageService.log(user, "CHAT", "gpt-4o", "nine-router",
                10, 20, 30, 100L, true, null, false);

        verify(repository).save(logCaptor.capture());
        var saved = logCaptor.getValue();
        assertThat(saved.getRequestType()).isEqualTo("CHAT");
        assertThat(saved.getTotalTokens()).isEqualTo(30);
        assertThat(saved.isSuccess()).isTrue();
    }

    @Test
    void getTokensUsedThisMonth_shouldReturnSum() {
        var since = OffsetDateTime.now().withDayOfMonth(1).withHour(0).withMinute(0).withSecond(0).withNano(0);
        when(repository.sumTokensByUserIdSince(eq(1L), any(OffsetDateTime.class))).thenReturn(500L);

        var result = usageService.getTokensUsedThisMonth(1L);
        assertThat(result).isEqualTo(500L);
    }

    @Test
    void hasQuota_shouldReturnTrueWhenUnderLimit() {
        when(repository.sumTokensByUserIdSince(eq(1L), any(OffsetDateTime.class))).thenReturn(100L);

        assertThat(usageService.hasQuota(1L, 1000)).isTrue();
    }

    @Test
    void log_shouldNotThrowOnFailure() {
        var user = User.builder().id(1L).build();
        when(repository.save(any())).thenThrow(new RuntimeException("DB error"));

        // must not throw - best-effort logging
        usageService.log(user, "CHAT", "gpt-4o", "nine-router",
                10, 20, 30, 100L, true, null, false);
    }
}
