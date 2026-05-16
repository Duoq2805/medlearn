package com.duoq.medlearn.domain.entity;

import com.duoq.medlearn.domain.enums.CaseDifficulty;
import com.duoq.medlearn.domain.enums.DiseaseStatus;
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
@SQLRestriction("is_deleted = false")
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

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by", nullable = false)
    private User createdBy;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private DiseaseStatus status = DiseaseStatus.DRAFT;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private OffsetDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private OffsetDateTime updatedAt;

    @Column(name = "is_deleted", nullable = false)
    @Builder.Default
    private Boolean isDeleted = false;

    @ManyToMany
    @JoinTable(
            name = "case_study_symptom",
            joinColumns = @JoinColumn(name = "case_study_id"),
            inverseJoinColumns = @JoinColumn(name = "symptom_id")
    )
    @Builder.Default
    private Set<Symptom> symptoms = new HashSet<>();
}