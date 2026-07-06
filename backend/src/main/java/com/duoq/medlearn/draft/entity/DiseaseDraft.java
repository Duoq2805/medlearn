package com.duoq.medlearn.draft.entity;

import com.duoq.medlearn.draft.enums.DraftMethod;
import com.duoq.medlearn.draft.enums.DraftStatus;
import com.duoq.medlearn.document.entity.Document;
import com.duoq.medlearn.domain.entity.Disease;
import com.duoq.medlearn.domain.entity.User;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.SQLRestriction;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.OffsetDateTime;

@Entity
@Table(name = "disease_draft")
@SQLRestriction("deleted_at IS NULL")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DiseaseDraft {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "disease_id")
    private Disease disease;

    @Column(nullable = false, length = 255)
    private String title;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @Builder.Default
    private DraftStatus status = DraftStatus.DRAFT;

    @Enumerated(EnumType.STRING)
    @Column(name = "source_method", nullable = false, length = 20)
    @Builder.Default
    private DraftMethod sourceMethod = DraftMethod.MANUAL;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "source_document_id")
    private Document sourceDocument;

    @Column(name = "ai_model", length = 50)
    private String aiModel;

    @Column(name = "ai_prompt_template_code", length = 100)
    private String aiPromptTemplateCode;

    @Column(name = "ai_total_tokens")
    @Builder.Default
    private Integer aiTotalTokens = 0;

    @Column(name = "ai_latency_ms")
    @Builder.Default
    private Integer aiLatencyMs = 0;

    @Column(name = "review_note", columnDefinition = "TEXT")
    private String reviewNote;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by", nullable = false)
    private User createdBy;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reviewed_by")
    private User reviewedBy;

    @Column(name = "reviewed_at")
    private OffsetDateTime reviewedAt;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private OffsetDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private OffsetDateTime updatedAt;

    @Column(name = "deleted_at")
    private OffsetDateTime deletedAt;
}
