package com.duoq.medlearn.quiz.dto.request;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;

@Getter
public class QuizGenerateRequest {

    private Long diseaseId;
    private Long documentId;

    @NotBlank
    private String title;

    @NotNull
    @Min(1) @Max(30)
    private Integer count = 10;

    private String model;
    private Double temperature;

    public QuizGenerateRequest() {}

    public QuizGenerateRequest(Long diseaseId, Long documentId, String title,
                               Integer count, String model, Double temperature) {
        this.diseaseId = diseaseId;
        this.documentId = documentId;
        this.title = title;
        this.count = count != null ? count : 10;
        this.model = model;
        this.temperature = temperature;
    }
}
