package com.duoq.medlearn.service;

public interface RealtimePublisher {
    void publishToUser(String channel, Long userId, String eventName, Object payload);
}
