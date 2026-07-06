package com.duoq.medlearn.ai.summary.entity;

import com.duoq.medlearn.ai.dto.enums.SummaryType;
import com.duoq.medlearn.domain.entity.Disease;
import com.duoq.medlearn.domain.entity.DiseaseVersion;
import com.duoq.medlearn.domain.entity.User;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.SQLRestriction;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.OffsetDateTime;

@Entity
@Table(name = "ai_summary",
        uniqueConstraints = @UniqueConstraint(columnNames = {"disease_id", "summary_type", "version"}))
@SQLRestriction("deleted_at IS NULL")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AiSummary {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "disease_id", nullable = false)
    private Disease disease;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "disease_version_id", nullable = false)
    private DiseaseVersion diseaseVersion;

    @Enumerated(EnumType.STRING)
    @Column(name = "summary_type", nullable = false, length = 50)
    private SummaryType summaryType;

    @Column(nullable = false)
    @Builder.Default
    private Integer version = 1;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String content;

    @Column(length = 100)
    private String model;

    @Column(length = 50)
    private String provider;

    @Column(name = "prompt_tokens", nullable = false)
    @Builder.Default
    private int promptTokens = 0;

    @Column(name = "completion_tokens", nullable = false)
    @Builder.Default
    private int completionTokens = 0;

    @Column(name = "total_tokens", nullable = false)
    @Builder.Default
    private int totalTokens = 0;

    @Column(name = "latency_ms")
    private Integer latencyMs;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by")
    private User createdBy;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private OffsetDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private OffsetDateTime updatedAt;

    @Column(name = "deleted_at")
    private OffsetDateTime deletedAt;
}
