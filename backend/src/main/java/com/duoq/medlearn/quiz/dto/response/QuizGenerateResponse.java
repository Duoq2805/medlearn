package com.duoq.medlearn.quiz.dto.response;

import com.duoq.medlearn.ai.dto.response.AiUsage;
import lombok.Builder;
import lombok.Value;

@Value
@Builder(toBuilder = true)
public class QuizGenerateResponse {
    Long quizId;
    String title;
    int totalGenerated;
    QuizResponse quiz;
    AiUsage usage;
}
