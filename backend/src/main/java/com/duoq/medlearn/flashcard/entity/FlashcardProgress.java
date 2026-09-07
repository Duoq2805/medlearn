package com.duoq.medlearn.flashcard.entity;

import com.duoq.medlearn.auth.entity.User;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.SQLRestriction;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.OffsetDateTime;

@Entity
@Table(name = "flashcard_progress",
        uniqueConstraints = @UniqueConstraint(columnNames = {"flashcard_id", "user_id"}))
@SQLRestriction("deleted_at IS NULL")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FlashcardProgress {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "flashcard_id", nullable = false)
    private Flashcard flashcard;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "easiness_factor", nullable = false)
    @Builder.Default
    private double easinessFactor = 2.5;

    @Column(name = "review_interval", nullable = false)
    @Builder.Default
    private int interval = 0;

    @Column(nullable = false)
    @Builder.Default
    private int repetitions = 0;

    @Column(name = "next_review_at")
    @Builder.Default
    private OffsetDateTime nextReviewAt = OffsetDateTime.now();

    @Column(name = "last_reviewed_at")
    private OffsetDateTime lastReviewedAt;

    @Column(name = "last_quality")
    private Integer lastQuality;

    @Column(name = "total_reviews")
    @Builder.Default
    private int totalReviews = 0;

    @Column(name = "correct_count")
    @Builder.Default
    private int correctCount = 0;

    @Column(name = "incorrect_count")
    @Builder.Default
    private int incorrectCount = 0;

    @Builder.Default
    private boolean mastered = false;

    @Version
    @Column(nullable = false)
    @Builder.Default
    private Integer version = 0;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private OffsetDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private OffsetDateTime updatedAt;

    @Column(name = "deleted_at")
    private OffsetDateTime deletedAt;
}
