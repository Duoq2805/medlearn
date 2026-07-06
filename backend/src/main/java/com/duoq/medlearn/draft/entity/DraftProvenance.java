package com.duoq.medlearn.draft.entity;

import com.duoq.medlearn.draft.enums.DraftSectionType;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.OffsetDateTime;

@Entity
@Table(name = "draft_provenance")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DraftProvenance {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "draft_id", nullable = false)
    private DiseaseDraft draft;

    @Enumerated(EnumType.STRING)
    @Column(name = "section_type", nullable = false, length = 50)
    private DraftSectionType sectionType;

    @Column(name = "system_prompt", columnDefinition = "TEXT")
    private String systemPrompt;

    @Column(name = "user_prompt", nullable = false, columnDefinition = "TEXT")
    private String userPrompt;

    @Column(name = "raw_response", nullable = false, columnDefinition = "TEXT")
    private String rawResponse;

    @Column(length = 50)
    private String model;

    @Column(length = 50)
    private String provider;

    @Column(name = "prompt_tokens")
    @Builder.Default
    private Integer promptTokens = 0;

    @Column(name = "completion_tokens")
    @Builder.Default
    private Integer completionTokens = 0;

    @Column(name = "total_tokens")
    @Builder.Default
    private Integer totalTokens = 0;

    @Column(name = "latency_ms")
    @Builder.Default
    private Integer latencyMs = 0;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private OffsetDateTime createdAt;
}
