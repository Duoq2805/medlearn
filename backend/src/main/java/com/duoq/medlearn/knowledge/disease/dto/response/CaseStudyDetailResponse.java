package com.duoq.medlearn.domain.dto.casestudy;

import com.duoq.medlearn.domain.enums.CaseDifficulty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.time.OffsetDateTime;
import java.util.Set;

@Data
@Builder
@AllArgsConstructor
public class CaseStudyDetailResponse {
    private Long id;
    private String title;
    private String slug;
    private String description;
    private CaseDifficulty difficulty;
    private String diagnosis;
    private String learningNotes;
    private Integer patientAge;
    private String patientGender;
    private String chiefComplaint;
    private String caseQuestion;
    private String explanation;
    private Long viewCount;
    private Boolean isFeatured;
    private Long createdBy;
    private OffsetDateTime createdAt;
    private OffsetDateTime updatedAt;
    private Set<Long> symptomIds;
}
