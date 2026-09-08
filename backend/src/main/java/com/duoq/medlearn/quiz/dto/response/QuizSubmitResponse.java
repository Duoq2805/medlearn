package com.duoq.medlearn.quiz.dto.response;

import lombok.Builder;
import lombok.Value;

import java.util.List;

@Value
@Builder(toBuilder = true)
public class QuizSubmitResponse {
    Long attemptId;
    Long quizId;
    int correctCount;
    int totalQuestions;
    double percentage;
    List<QuestionResult> results;

    @Value
    @Builder(toBuilder = true)
    public static class QuestionResult {
        Long questionId;
        String selectedAnswer;
        String correctAnswer;
        boolean correct;
        String explanation;
    }
}
