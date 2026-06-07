package com.duoq.medlearn.domain.dto.response;

import lombok.Builder;
import lombok.Data;

import java.time.OffsetDateTime;

@Data
@Builder
public class NotificationEventResponse {
    private String type;
    private String title;
    private String message;
    private OffsetDateTime createdAt;
}
