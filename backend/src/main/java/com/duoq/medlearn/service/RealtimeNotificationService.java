package com.duoq.medlearn.service;

import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

public interface RealtimeNotificationService {
    SseEmitter subscribeNotifications();

    SseEmitter subscribeAiStream();

    void sendNotificationToUser(Long userId, String type, String title, String message);

    void sendAiChunkToUser(Long userId, String chunk, boolean done);
}
