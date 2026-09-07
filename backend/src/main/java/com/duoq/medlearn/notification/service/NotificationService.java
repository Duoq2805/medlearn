package com.duoq.medlearn.notification.service;

import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

public interface NotificationService {

    // SSE channels
    String CHANNEL_NOTIFICATION = "notification";
    String CHANNEL_AI_STREAM = "ai-stream";

    // Subscribe
    SseEmitter subscribeNotifications();
    SseEmitter subscribeAiStream();

    // Publish — high-level helpers
    void sendNotificationToUser(Long userId, String type, String title, String message);
    void sendAiChunkToUser(Long userId, String chunk, boolean done);

    // Publish — low-level (used by other services)
    void publishToUser(String channel, Long userId, String eventName, Object payload);
}
