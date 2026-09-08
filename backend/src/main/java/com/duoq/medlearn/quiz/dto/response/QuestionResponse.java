package com.duoq.medlearn.quiz.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Value;

import java.time.OffsetDateTime;

@Value
@Builder(toBuilder = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class QuestionResponse {
    Long id;
    Long quizId;
    String content;
    String optionA;
    String optionB;
    String optionC;
    String optionD;
    // correctAnswer intentionally omitted from normal responses — revealed only after submit
    String explanation;
    Integer displayOrder;
    OffsetDateTime createdAt;
}
