package com.duoq.medlearn.flashcard.dto.request;

import com.duoq.medlearn.flashcard.enums.FlashcardDifficulty;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;

@Getter
public class FlashcardGenerateRequest {

    private Long diseaseId;
    private Long documentId;

    @NotBlank
    private String title;

    @Min(1) @Max(50)
    private Integer count = 10;

    private FlashcardDifficulty difficulty = FlashcardDifficulty.MEDIUM;

    private String tag;

    private String model;

    private Double temperature;

    public FlashcardGenerateRequest() {}

    public FlashcardGenerateRequest(Long diseaseId, Long documentId, String title,
                                    Integer count, FlashcardDifficulty difficulty,
                                    String tag, String model, Double temperature) {
        this.diseaseId = diseaseId;
        this.documentId = documentId;
        this.title = title;
        this.count = count != null ? count : 10;
        this.difficulty = difficulty != null ? difficulty : FlashcardDifficulty.MEDIUM;
        this.tag = tag;
        this.model = model;
        this.temperature = temperature;
    }
}
