package com.duoq.medlearn.ai.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.Map;

@Data
@Component
@ConfigurationProperties(prefix = "ai")
public class AiProperties {

    private Gateway gateway = new Gateway();
    private Prompt prompt = new Prompt();
    private Cache cache = new Cache();
    private Quota quota = new Quota();
    private RateLimit rateLimit = new RateLimit();
    private Security security = new Security();

    @Data
    public static class Gateway {
        private String defaultProvider = "nine-router";
        private String apiKey;
        private String baseUrl;
        private int timeoutMs = 60000;
        private int maxRetries = 2;
        private Map<String, ProviderConfig> providers;
    }

    @Data
    public static class ProviderConfig {
        private String apiKey;
        private String baseUrl;
        private String defaultModel;
        private int timeoutMs;
        private boolean enabled = true;
    }

    @Data
    public static class Prompt {
        private String location = "prompts";
        private String encoding = "UTF-8";
        private int maxVersionHistory = 20;
    }

    @Data
    public static class Cache {
        private boolean enabled = true;
        private int maxSize = 1000;
        private int ttlMinutes = 60;
        private int maxEntrySize = 10000;
    }

    @Data
    public static class Quota {
        private long defaultMonthlyTokens = 1_000_000L;
        private boolean enabled = true;
    }

    @Data
    public static class RateLimit {
        private boolean enabled = true;
        private int requestsPerMinute = 100;
        private int tokensPerMinute = 100_000;
    }

    @Data
    public static class Security {
        private boolean injectionFilterEnabled = true;
        private boolean outputFilterEnabled = true;
        private int maxPromptLength = 100_000;
        private int minPromptLength = 1;
    }
}
