package com.duoq.medlearn.quiz.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.Value;

import java.util.List;

@Value
public class QuizSubmitRequest {

    @NotEmpty
    @Valid
    List<Answer> answers;

    @Value
    public static class Answer {
        @NotNull
        Long questionId;

        @NotNull
        @Pattern(regexp = "[A-D]", message = "selectedAnswer must be A, B, C, or D")
        String selectedAnswer;
    }
}
