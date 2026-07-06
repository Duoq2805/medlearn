package com.duoq.medlearn.draft.entity;

import com.duoq.medlearn.document.entity.DocumentChunk;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.OffsetDateTime;

@Entity
@Table(name = "draft_source")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DraftSource {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "draft_section_id", nullable = false)
    private DiseaseDraftSection draftSection;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "document_chunk_id", nullable = false)
    private DocumentChunk documentChunk;

    @Column(name = "relevance_score")
    @Builder.Default
    private Double relevanceScore = 0.0;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private OffsetDateTime createdAt;
}
