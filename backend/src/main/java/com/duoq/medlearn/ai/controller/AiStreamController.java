package com.duoq.medlearn.ai.controller;

import com.duoq.medlearn.notification.service.NotificationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

@RestController
@RequestMapping("/api/ai")
@RequiredArgsConstructor
@Tag(name = "AI Stream", description = "AI streaming APIs")
public class AiStreamController {

    private final NotificationService notificationService;

    @GetMapping(value = "/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    @Operation(summary = "Open AI SSE stream", description = "Returns text/event-stream. This endpoint is not wrapped in ApiResponse because SSE requires streaming frames.")
    public SseEmitter streamAiStream() {
        return notificationService.subscribeAiStream();
    }
}
