package com.duoq.medlearn.domain.entity;

import com.duoq.medlearn.domain.enums.VersionStatus;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.SQLRestriction;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.OffsetDateTime;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Set;

@Entity
@Table(
        name = "disease_version",
        uniqueConstraints = {
                @UniqueConstraint(
                        columnNames = {"disease_id", "version_number"}
                )
        }
)
@SQLRestriction("deleted_at IS NULL")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DiseaseVersion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "disease_id", nullable = false)
    private Disease disease;

    @Column(name = "version_number", nullable = false)
    private Integer versionNumber;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by", nullable = false)
    private User createdBy;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    @Builder.Default
    private VersionStatus status = VersionStatus.DRAFT;

    @Column(name = "moderation_note", columnDefinition = "TEXT")
    private String moderationNote;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reviewed_by")
    private User reviewedBy;

    @Column(name = "reviewed_at")
    private OffsetDateTime reviewedAt;

    @Column(name = "deleted_at")
    private OffsetDateTime deletedAt;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private OffsetDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private OffsetDateTime updatedAt;

    @OneToMany(
            mappedBy = "diseaseVersion",
            cascade = CascadeType.ALL,
            orphanRemoval = true
    )
    @OrderBy("orderIndex ASC")
    @Builder.Default
    private Set<DiseaseSection> sections = new LinkedHashSet<>();

    @OneToMany(
            mappedBy = "diseaseVersion",
            cascade = CascadeType.ALL,
            orphanRemoval = true
    )
    @Builder.Default
    private Set<DiseaseVersionSymptom> symptoms = new HashSet<>();

    @Version
    @Column(nullable = false)
    @Builder.Default
    private Integer version = 0;

    public boolean isApproved() {
        return this.status == VersionStatus.APPROVED;
    }

    public boolean isDraft() {
        return this.status == VersionStatus.DRAFT;
    }

    public boolean isPending() {
        return this.status == VersionStatus.PENDING_REVIEW;
    }

    public void approve(User reviewer) {
        this.status = VersionStatus.APPROVED;
        this.reviewedBy = reviewer;
        this.reviewedAt = OffsetDateTime.now();
    }

    public void reject(User reviewer, String reason) {
        this.status = VersionStatus.REJECTED;
        this.reviewedBy = reviewer;
        this.reviewedAt = OffsetDateTime.now();
        this.moderationNote = reason;
    }

    public void submitForReview() {
        this.status = VersionStatus.PENDING_REVIEW;
    }
}
