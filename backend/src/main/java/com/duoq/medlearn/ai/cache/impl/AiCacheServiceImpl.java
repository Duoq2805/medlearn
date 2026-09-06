package com.duoq.medlearn.ai.gateway;

import com.duoq.medlearn.ai.config.AiProperties;
import com.duoq.medlearn.ai.dto.request.AiChatRequest;
import com.duoq.medlearn.ai.dto.response.AiChatResponse;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

import java.security.MessageDigest;
import java.util.HexFormat;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

@Service
@ConditionalOnProperty(name = "ai.cache.enabled", havingValue = "true", matchIfMissing = true)
@RequiredArgsConstructor
@Slf4j
public class AiCacheServiceImpl implements AiCacheService {

    private final AiProperties aiProperties;
    private final Map<String, CacheEntry> cache = new ConcurrentHashMap<>();

    @PostConstruct
    void init() {
        log.info("AI cache initialized: maxSize={}, ttlMinutes={}",
                aiProperties.getCache().getMaxSize(), aiProperties.getCache().getTtlMinutes());
    }

    @Override
    public Optional<AiChatResponse> get(AiChatRequest request) {
        var key = buildCacheKey(request);
        var entry = cache.get(key);
        if (entry == null) return Optional.empty();

        if (System.currentTimeMillis() > entry.expiresAt()) {
            cache.remove(key);
            return Optional.empty();
        }

        log.debug("AI cache hit: key={}", key);
        return Optional.of(entry.response());
    }

    @Override
    public void put(AiChatRequest request, AiChatResponse response) {
        if (cache.size() >= aiProperties.getCache().getMaxSize()) {
            cache.clear();
            log.debug("AI cache cleared due to max size");
        }

        var key = buildCacheKey(request);
        var ttlMs = aiProperties.getCache().getTtlMinutes() * 60_000L;
        cache.put(key, new CacheEntry(response, System.currentTimeMillis() + ttlMs));
    }

    @Override
    public void evict(String cacheKey) {
        cache.remove(cacheKey);
    }

    @Override
    public void clear() {
        cache.clear();
    }

    @Override
    public long size() {
        return cache.size();
    }

    private String buildCacheKey(AiChatRequest request) {
        try {
            var md = MessageDigest.getInstance("SHA-256");
            var content = String.format("%s|%s|%s",
                    request.getModel(),
                    request.getTemperature(),
                    request.getMessages() != null ? request.getMessages().toString() : "");
            md.update(content.getBytes());
            return HexFormat.of().formatHex(md.digest());
        } catch (Exception e) {
            return String.valueOf(request.hashCode());
        }
    }

    private record CacheEntry(AiChatResponse response, long expiresAt) {}
}
