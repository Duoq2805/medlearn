package com.duoq.medlearn.domain.entity;

import com.duoq.medlearn.domain.enums.CaseDifficulty;
import com.duoq.medlearn.domain.enums.ContentStatus;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.SQLRestriction;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.OffsetDateTime;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "case_study")
@SQLRestriction("deleted_at IS NULL")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CaseStudy {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 255)
    private String title;

    @Column(nullable = false, unique = true, length = 255)
    private String slug;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private CaseDifficulty difficulty = CaseDifficulty.MEDIUM;

    @Column(columnDefinition = "TEXT")
    private String diagnosis;

    @Column(name = "learning_notes", columnDefinition = "TEXT")
    private String learningNotes;

    @Column(name = "patient_age")
    private Integer patientAge;

    @Column(name = "patient_gender", length = 20)
    private String patientGender;

    @Column(name = "chief_complaint", columnDefinition = "TEXT")
    private String chiefComplaint;

    @Column(name = "case_question", columnDefinition = "TEXT")
    private String caseQuestion;

    @Column(name = "explanation", columnDefinition = "TEXT")
    private String explanation;

    @Column(name = "view_count")
    @Builder.Default
    private Long viewCount = 0L;

    @Column(name = "is_featured")
    @Builder.Default
    private Boolean isFeatured = false;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by", nullable = false)
    private User createdBy;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private ContentStatus status = ContentStatus.DRAFT;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private OffsetDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private OffsetDateTime updatedAt;

    @Column(name = "deleted_at")
    private OffsetDateTime deletedAt;

    @ManyToMany
    @JoinTable(
            name = "case_study_symptom",
            joinColumns = @JoinColumn(name = "case_study_id"),
            inverseJoinColumns = @JoinColumn(name = "symptom_id")
    )
    @Builder.Default
    private Set<Symptom> symptoms = new HashSet<>();

    // Helper methods
    public boolean isPublic() {
        return this.status == ContentStatus.APPROVED;
    }

    public boolean isEditable() {
        return this.status == ContentStatus.DRAFT;
    }

    public void approve() {
        this.status = ContentStatus.APPROVED;
    }

    public void archive() {
        this.status = ContentStatus.ARCHIVED;
    }

    public void incrementViewCount() {
        this.viewCount++;
    }
}