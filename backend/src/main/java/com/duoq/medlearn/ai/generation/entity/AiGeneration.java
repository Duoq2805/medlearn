package com.duoq.medlearn.ai.generation.entity;

import com.duoq.medlearn.ai.enums.FeatureType;
import com.duoq.medlearn.auth.entity.User;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.OffsetDateTime;

@Entity
@Table(name = "ai_generation")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AiGeneration {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(name = "feature_type", nullable = false, length = 30)
    private FeatureType featureType;

    @Column(name = "feature_id", nullable = false)
    private Long featureId;

    @Column(name = "prompt_template_code", length = 100)
    private String promptTemplateCode;

    @Column(name = "prompt_template_version", length = 20)
    private String promptTemplateVersion;

    @Column(length = 100)
    private String model;

    @Column(length = 50)
    private String provider;

    @Column(name = "prompt_tokens")
    @Builder.Default
    private int promptTokens = 0;

    @Column(name = "completion_tokens")
    @Builder.Default
    private int completionTokens = 0;

    @Column(name = "total_tokens")
    @Builder.Default
    private int totalTokens = 0;

    @Column(name = "latency_ms")
    private Integer latencyMs;

    @Column(name = "system_prompt", columnDefinition = "TEXT")
    private String systemPrompt;

    @Column(name = "user_prompt", columnDefinition = "TEXT")
    private String userPrompt;

    @Column(name = "raw_response", columnDefinition = "TEXT")
    private String rawResponse;

    @Column(length = 20)
    @Builder.Default
    private String status = "SUCCESS";

    @Column(name = "error_message", columnDefinition = "TEXT")
    private String errorMessage;

    @Column(name = "metadata", columnDefinition = "TEXT")
    private String metadata;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by")
    private User createdBy;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private OffsetDateTime createdAt;
}
