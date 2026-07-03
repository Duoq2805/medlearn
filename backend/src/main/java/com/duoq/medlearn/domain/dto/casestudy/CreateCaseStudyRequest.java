package com.duoq.medlearn.domain.dto.casestudy;

import com.duoq.medlearn.domain.enums.CaseDifficulty;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.util.Set;

@Data
public class CreateCaseStudyRequest {
    @NotBlank(message = "Title is required")
    private String title;

    private String description;
    private CaseDifficulty difficulty;
    private String diagnosis;
    private String learningNotes;
    private Integer patientAge;
    private String patientGender;
    private String chiefComplaint;
    private String caseQuestion;
    private String explanation;
    private Set<Long> symptomIds;
}
