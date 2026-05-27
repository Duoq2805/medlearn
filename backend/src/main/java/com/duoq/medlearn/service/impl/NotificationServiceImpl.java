package com.duoq.medlearn.service.impl;

import com.duoq.medlearn.dto.response.AiStreamChunkResponse;
import com.duoq.medlearn.dto.response.NotificationEventResponse;
import com.duoq.medlearn.security.CurrentUserResolver;
import com.duoq.medlearn.service.NotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

@Service
@Slf4j
@RequiredArgsConstructor
public class NotificationServiceImpl implements NotificationService {

    private static final long SSE_TIMEOUT = 30L * 60L * 1000L;

    private final CurrentUserResolver currentUserResolver;

    private final Map<Long, CopyOnWriteArrayList<SseEmitter>> notificationEmitters = new ConcurrentHashMap<>();
    private final Map<Long, CopyOnWriteArrayList<SseEmitter>> aiEmitters = new ConcurrentHashMap<>();

    @Override
    public SseEmitter subscribeNotifications() {
        return subscribe(NotificationService.CHANNEL_NOTIFICATION, "notification-connected");
    }

    @Override
    public SseEmitter subscribeAiStream() {
        return subscribe(NotificationService.CHANNEL_AI_STREAM, "ai-stream-connected");
    }

    @Override
    public void sendNotificationToUser(Long userId, String type, String title, String message) {
        NotificationEventResponse payload = NotificationEventResponse.builder()
                .type(type)
                .title(title)
                .message(message)
                .createdAt(OffsetDateTime.now())
                .build();

        publishToUser(NotificationService.CHANNEL_NOTIFICATION, userId, "notification", payload);
    }

    @Override
    public void sendAiChunkToUser(Long userId, String chunk, boolean done) {
        AiStreamChunkResponse payload = AiStreamChunkResponse.builder()
                .chunk(chunk)
                .done(done)
                .createdAt(OffsetDateTime.now())
                .build();

        publishToUser(NotificationService.CHANNEL_AI_STREAM, userId, "ai-stream", payload);
    }

    @Override
    public void publishToUser(String channel, Long userId, String eventName, Object payload) {
        Map<Long, CopyOnWriteArrayList<SseEmitter>> store = getStoreByChannel(channel);
        broadcast(store, userId, eventName, payload);
    }

    private SseEmitter subscribe(String channel, String connectedEventName) {
        Long userId = currentUserResolver.resolveCurrentUserId();
        SseEmitter emitter = new SseEmitter(SSE_TIMEOUT);

        Map<Long, CopyOnWriteArrayList<SseEmitter>> store = getStoreByChannel(channel);
        store.computeIfAbsent(userId, key -> new CopyOnWriteArrayList<>()).add(emitter);

        emitter.onCompletion(() -> removeEmitter(store, userId, emitter));
        emitter.onTimeout(() -> removeEmitter(store, userId, emitter));
        emitter.onError(error -> removeEmitter(store, userId, emitter));

        sendEvent(store, userId, emitter, connectedEventName, Map.of("connected", true));
        return emitter;
    }

    private void broadcast(Map<Long, CopyOnWriteArrayList<SseEmitter>> store,
                           Long userId,
                           String eventName,
                           Object payload) {
        List<SseEmitter> emitters = store.get(userId);
        if (emitters == null || emitters.isEmpty()) {
            return;
        }

        for (SseEmitter emitter : emitters) {
            sendEvent(store, userId, emitter, eventName, payload);
        }
    }

    private void sendEvent(Map<Long, CopyOnWriteArrayList<SseEmitter>> store,
                           Long userId,
                           SseEmitter emitter,
                           String eventName,
                           Object payload) {
        try {
            emitter.send(SseEmitter.event().name(eventName).data(payload));
        } catch (IOException e) {
            removeEmitter(store, userId, emitter);
            emitter.completeWithError(e);
            log.debug("SSE emitter removed for user {} on event {}", userId, eventName);
        }
    }

    private void removeEmitter(Map<Long, CopyOnWriteArrayList<SseEmitter>> store,
                               Long userId,
                               SseEmitter emitter) {
        List<SseEmitter> emitters = store.get(userId);
        if (emitters == null) {
            return;
        }

        emitters.remove(emitter);
        if (emitters.isEmpty()) {
            store.remove(userId);
        }
    }

    private Map<Long, CopyOnWriteArrayList<SseEmitter>> getStoreByChannel(String channel) {
        if (NotificationService.CHANNEL_NOTIFICATION.equals(channel)) {
            return notificationEmitters;
        }
        if (NotificationService.CHANNEL_AI_STREAM.equals(channel)) {
            return aiEmitters;
        }
        throw new IllegalArgumentException("Unsupported realtime channel: " + channel);
    }
}
