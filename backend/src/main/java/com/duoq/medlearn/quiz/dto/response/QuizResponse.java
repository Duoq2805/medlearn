package com.duoq.medlearn.quiz.dto.response;

import lombok.Builder;
import lombok.Value;

import java.time.OffsetDateTime;
import java.util.List;

@Value
@Builder(toBuilder = true)
public class QuizResponse {
    Long id;
    String title;
    String sourceType;
    Long sourceId;
    Integer questionCount;
    String status;
    Long createdBy;
    List<QuestionResponse> questions;
    OffsetDateTime createdAt;
    OffsetDateTime updatedAt;
}
