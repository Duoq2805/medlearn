package com.duoq.medlearn.quiz.dto.response;

import lombok.Builder;
import lombok.Value;

import java.time.OffsetDateTime;
import java.util.List;

@Value
@Builder(toBuilder = true)
public class QuizAttemptResponse {
    Long id;
    Long quizId;
    String quizTitle;
    int correctCount;
    int totalQuestions;
    double percentage;
    String status;
    OffsetDateTime createdAt;
}
